package com.cartaxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cartaxi.common.exception.BusinessException;
import com.cartaxi.common.util.DateTimeUtil;
import com.cartaxi.dto.admin.order.RentalOrderQueryDTO;
import com.cartaxi.dto.admin.order.RentalOrderSaveDTO;
import com.cartaxi.entity.CarInfo;
import com.cartaxi.entity.Region;
import com.cartaxi.entity.RentalOrder;
import com.cartaxi.entity.SysUser;
import com.cartaxi.mapper.CarInfoMapper;
import com.cartaxi.mapper.RegionMapper;
import com.cartaxi.mapper.RentalOrderMapper;
import com.cartaxi.mapper.SysUserMapper;
import com.cartaxi.service.AdminRentalOrderService;
import com.cartaxi.vo.admin.deploy.CarSimpleOptionVO;
import com.cartaxi.vo.admin.order.RentalOrderFormOptionsVO;
import com.cartaxi.vo.admin.order.RentalOrderVO;
import com.cartaxi.vo.admin.order.UserSimpleOptionVO;
import com.cartaxi.vo.admin.region.RegionOptionVO;
import com.cartaxi.vo.common.PageResult;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminRentalOrderServiceImpl implements AdminRentalOrderService {

    private static final Set<String> ALLOWED_ORDER_STATUS = Set.of("CREATED", "PAID", "USING", "RETURNING", "COMPLETED", "CANCELLED");
    private static final Set<String> ALLOWED_PAYMENT_STATUS = Set.of("UNPAID", "PAID", "REFUNDED");
    private static final DateTimeFormatter ORDER_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final RentalOrderMapper rentalOrderMapper;
    private final SysUserMapper sysUserMapper;
    private final CarInfoMapper carInfoMapper;
    private final RegionMapper regionMapper;

    public AdminRentalOrderServiceImpl(RentalOrderMapper rentalOrderMapper,
                                       SysUserMapper sysUserMapper,
                                       CarInfoMapper carInfoMapper,
                                       RegionMapper regionMapper) {
        this.rentalOrderMapper = rentalOrderMapper;
        this.sysUserMapper = sysUserMapper;
        this.carInfoMapper = carInfoMapper;
        this.regionMapper = regionMapper;
    }

    @Override
    public PageResult<RentalOrderVO> page(RentalOrderQueryDTO queryDTO) {
        List<Long> userIds = findMatchedUserIds(queryDTO.getUserKeyword());
        if (StringUtils.hasText(queryDTO.getUserKeyword()) && userIds.isEmpty()) {
            return PageResult.empty(queryDTO.getPageNum(), queryDTO.getPageSize());
        }
        List<Long> carIds = findMatchedCarIds(queryDTO.getCarKeyword());
        if (StringUtils.hasText(queryDTO.getCarKeyword()) && carIds.isEmpty()) {
            return PageResult.empty(queryDTO.getPageNum(), queryDTO.getPageSize());
        }

        Page<RentalOrder> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<RentalOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getOrderNo()), RentalOrder::getOrderNo, queryDTO.getOrderNo())
                .in(userIds != null && !userIds.isEmpty(), RentalOrder::getUserId, userIds)
                .in(carIds != null && !carIds.isEmpty(), RentalOrder::getCarId, carIds)
                .eq(StringUtils.hasText(queryDTO.getOrderStatus()), RentalOrder::getOrderStatus, queryDTO.getOrderStatus())
                .eq(StringUtils.hasText(queryDTO.getPaymentStatus()), RentalOrder::getPaymentStatus, queryDTO.getPaymentStatus())
                .orderByDesc(RentalOrder::getCreateTime);
        Page<RentalOrder> result = rentalOrderMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), enrich(result.getRecords()));
    }

    @Override
    public RentalOrderVO detail(Long id) {
        return enrich(getOrderOrThrow(id));
    }

    @Override
    public void create(RentalOrderSaveDTO saveDTO) {
        validateSaveDTO(saveDTO);
        RentalOrder order = new RentalOrder();
        BeanUtils.copyProperties(saveDTO, order, "startTime", "expectedReturnTime", "actualReturnTime");
        order.setOrderNo(generateOrderNo());
        applyDateFields(order, saveDTO);
        rentalOrderMapper.insert(order);
        syncCarState(order);
    }

    @Override
    public void update(Long id, RentalOrderSaveDTO saveDTO) {
        RentalOrder order = getOrderOrThrow(id);
        validateSaveDTO(saveDTO);
        BeanUtils.copyProperties(saveDTO, order, "orderNo", "startTime", "expectedReturnTime", "actualReturnTime");
        applyDateFields(order, saveDTO);
        rentalOrderMapper.updateById(order);
        syncCarState(order);
    }

    @Override
    public void delete(Long id) {
        getOrderOrThrow(id);
        rentalOrderMapper.deleteById(id);
    }

    @Override
    public void changeStatus(Long id, String status) {
        validateOrderStatus(status);
        RentalOrder order = getOrderOrThrow(id);
        order.setOrderStatus(status);
        rentalOrderMapper.updateById(order);
        syncCarState(order);
    }

    @Override
    public RentalOrderFormOptionsVO formOptions() {
        List<UserSimpleOptionVO> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getStatus, 1)
                        .orderByAsc(SysUser::getUsername))
                .stream()
                .map(item -> new UserSimpleOptionVO(
                        item.getId(),
                        item.getUsername(),
                        item.getRealName(),
                        item.getPhone(),
                        item.getUsername() + " / " + (StringUtils.hasText(item.getRealName()) ? item.getRealName() : "未实名")
                ))
                .collect(Collectors.toList());
        List<CarSimpleOptionVO> cars = carInfoMapper.selectList(new LambdaQueryWrapper<CarInfo>().orderByAsc(CarInfo::getCarCode))
                .stream()
                .map(item -> new CarSimpleOptionVO(
                        item.getId(),
                        item.getCarCode(),
                        item.getLicensePlate(),
                        item.getCarCode() + " / " + item.getLicensePlate() + " / " + item.getBrand() + " " + item.getModel()
                ))
                .collect(Collectors.toList());
        List<RegionOptionVO> regions = regionMapper.selectList(new LambdaQueryWrapper<Region>()
                        .eq(Region::getStatus, 1)
                        .orderByAsc(Region::getRegionCode))
                .stream()
                .map(item -> new RegionOptionVO(item.getId(), item.getRegionName(), item.getRegionCode()))
                .collect(Collectors.toList());
        return new RentalOrderFormOptionsVO(users, cars, regions);
    }

    private RentalOrder getOrderOrThrow(Long id) {
        RentalOrder order = rentalOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return order;
    }

    private void validateSaveDTO(RentalOrderSaveDTO saveDTO) {
        validateOrderStatus(saveDTO.getOrderStatus());
        validatePaymentStatus(saveDTO.getPaymentStatus());
        if (sysUserMapper.selectById(saveDTO.getUserId()) == null) {
            throw new BusinessException(400, "用户不存在");
        }
        if (carInfoMapper.selectById(saveDTO.getCarId()) == null) {
            throw new BusinessException(400, "车辆不存在");
        }
        if (regionMapper.selectById(saveDTO.getPickupRegionId()) == null) {
            throw new BusinessException(400, "取车区域不存在");
        }
        if (saveDTO.getReturnRegionId() != null && regionMapper.selectById(saveDTO.getReturnRegionId()) == null) {
            throw new BusinessException(400, "还车区域不存在");
        }

        LocalDateTime startTime = DateTimeUtil.parseDateTime(saveDTO.getStartTime(), "租赁开始时间");
        LocalDateTime expectedReturnTime = DateTimeUtil.parseDateTime(saveDTO.getExpectedReturnTime(), "预计归还时间");
        if (expectedReturnTime.isBefore(startTime)) {
            throw new BusinessException(400, "预计归还时间不能早于租赁开始时间");
        }
        if (StringUtils.hasText(saveDTO.getActualReturnTime())) {
            LocalDateTime actualReturnTime = DateTimeUtil.parseDateTime(saveDTO.getActualReturnTime(), "实际归还时间");
            if (actualReturnTime.isBefore(startTime)) {
                throw new BusinessException(400, "实际归还时间不能早于租赁开始时间");
            }
        }
    }

    private void validateOrderStatus(String status) {
        if (!StringUtils.hasText(status) || !ALLOWED_ORDER_STATUS.contains(status)) {
            throw new BusinessException(400, "订单状态不合法");
        }
    }

    private void validatePaymentStatus(String status) {
        if (!StringUtils.hasText(status) || !ALLOWED_PAYMENT_STATUS.contains(status)) {
            throw new BusinessException(400, "支付状态不合法");
        }
    }

    private void applyDateFields(RentalOrder order, RentalOrderSaveDTO saveDTO) {
        order.setStartTime(DateTimeUtil.parseDateTime(saveDTO.getStartTime(), "租赁开始时间"));
        order.setExpectedReturnTime(DateTimeUtil.parseDateTime(saveDTO.getExpectedReturnTime(), "预计归还时间"));
        order.setActualReturnTime(StringUtils.hasText(saveDTO.getActualReturnTime())
                ? DateTimeUtil.parseDateTime(saveDTO.getActualReturnTime(), "实际归还时间") : null);
    }

    private void syncCarState(RentalOrder order) {
        CarInfo carInfo = carInfoMapper.selectById(order.getCarId());
        if (carInfo == null) {
            return;
        }
        switch (order.getOrderStatus()) {
            case "USING" -> carInfo.setStatus("RENTING");
            case "RETURNING" -> carInfo.setStatus("RETURN_PENDING");
            case "COMPLETED" -> {
                carInfo.setCurrentRegionId(order.getReturnRegionId());
                carInfo.setStatus(order.getReturnRegionId() == null ? "IDLE" : "DEPLOYED");
            }
            case "CANCELLED", "CREATED", "PAID" -> carInfo.setStatus(carInfo.getCurrentRegionId() == null ? "IDLE" : "DEPLOYED");
            default -> {
            }
        }
        carInfoMapper.updateById(carInfo);
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(ORDER_NO_TIME_FORMATTER) + String.format("%03d", new Random().nextInt(1000));
    }

    private List<RentalOrderVO> enrich(List<RentalOrder> orders) {
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, SysUser> userMap = sysUserMapper.selectBatchIds(orders.stream().map(RentalOrder::getUserId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(SysUser::getId, item -> item));
        Map<Long, CarInfo> carMap = carInfoMapper.selectBatchIds(orders.stream().map(RentalOrder::getCarId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(CarInfo::getId, item -> item));
        List<Long> regionIds = orders.stream()
                .flatMap(item -> java.util.stream.Stream.of(item.getPickupRegionId(), item.getReturnRegionId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Region> regionMap = regionIds.isEmpty()
                ? Collections.emptyMap()
                : regionMapper.selectBatchIds(regionIds).stream().collect(Collectors.toMap(Region::getId, item -> item));
        return orders.stream().map(item -> toVO(item, userMap, carMap, regionMap)).collect(Collectors.toList());
    }

    private RentalOrderVO enrich(RentalOrder order) {
        Map<Long, SysUser> userMap = new HashMap<>();
        SysUser user = sysUserMapper.selectById(order.getUserId());
        if (user != null) {
            userMap.put(user.getId(), user);
        }
        Map<Long, CarInfo> carMap = new HashMap<>();
        CarInfo car = carInfoMapper.selectById(order.getCarId());
        if (car != null) {
            carMap.put(car.getId(), car);
        }
        Map<Long, Region> regionMap = new HashMap<>();
        Region pickup = regionMapper.selectById(order.getPickupRegionId());
        if (pickup != null) {
            regionMap.put(pickup.getId(), pickup);
        }
        if (order.getReturnRegionId() != null) {
            Region returned = regionMapper.selectById(order.getReturnRegionId());
            if (returned != null) {
                regionMap.put(returned.getId(), returned);
            }
        }
        return toVO(order, userMap, carMap, regionMap);
    }

    private RentalOrderVO toVO(RentalOrder order,
                               Map<Long, SysUser> userMap,
                               Map<Long, CarInfo> carMap,
                               Map<Long, Region> regionMap) {
        SysUser user = userMap.get(order.getUserId());
        CarInfo car = carMap.get(order.getCarId());
        Region pickup = regionMap.get(order.getPickupRegionId());
        Region returned = regionMap.get(order.getReturnRegionId());
        return RentalOrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .username(user == null ? null : user.getUsername())
                .realName(user == null ? null : user.getRealName())
                .phone(user == null ? null : user.getPhone())
                .carId(order.getCarId())
                .carCode(car == null ? null : car.getCarCode())
                .licensePlate(car == null ? null : car.getLicensePlate())
                .brand(car == null ? null : car.getBrand())
                .model(car == null ? null : car.getModel())
                .pickupRegionId(order.getPickupRegionId())
                .pickupRegionName(pickup == null ? null : pickup.getRegionName())
                .returnRegionId(order.getReturnRegionId())
                .returnRegionName(returned == null ? null : returned.getRegionName())
                .orderStatus(order.getOrderStatus())
                .orderAmount(order.getOrderAmount())
                .depositAmount(order.getDepositAmount())
                .startTime(order.getStartTime())
                .expectedReturnTime(order.getExpectedReturnTime())
                .actualReturnTime(order.getActualReturnTime())
                .paymentStatus(order.getPaymentStatus())
                .remark(order.getRemark())
                .createTime(order.getCreateTime())
                .updateTime(order.getUpdateTime())
                .build();
    }

    private List<Long> findMatchedUserIds(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .and(wrapper -> wrapper.like(SysUser::getUsername, keyword)
                                .or()
                                .like(SysUser::getRealName, keyword)
                                .or()
                                .like(SysUser::getPhone, keyword)))
                .stream().map(SysUser::getId).toList();
    }

    private List<Long> findMatchedCarIds(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return carInfoMapper.selectList(new LambdaQueryWrapper<CarInfo>()
                        .and(wrapper -> wrapper.like(CarInfo::getCarCode, keyword)
                                .or()
                                .like(CarInfo::getLicensePlate, keyword)))
                .stream().map(CarInfo::getId).toList();
    }
}