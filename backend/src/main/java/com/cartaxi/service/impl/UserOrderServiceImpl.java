package com.cartaxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cartaxi.common.context.UserContext;
import com.cartaxi.common.exception.BusinessException;
import com.cartaxi.common.util.DateTimeUtil;
import com.cartaxi.dto.user.order.UserOrderCreateDTO;
import com.cartaxi.dto.user.order.UserOrderQueryDTO;
import com.cartaxi.entity.CarInfo;
import com.cartaxi.entity.Region;
import com.cartaxi.entity.RentalOrder;
import com.cartaxi.entity.SysUser;
import com.cartaxi.mapper.CarInfoMapper;
import com.cartaxi.mapper.RegionMapper;
import com.cartaxi.mapper.RentalOrderMapper;
import com.cartaxi.mapper.SysUserMapper;
import com.cartaxi.service.UserOrderService;
import com.cartaxi.vo.admin.deploy.CarSimpleOptionVO;
import com.cartaxi.vo.admin.region.RegionOptionVO;
import com.cartaxi.vo.common.PageResult;
import com.cartaxi.vo.user.order.UserOrderFormOptionsVO;
import com.cartaxi.vo.user.order.UserOrderVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserOrderServiceImpl implements UserOrderService {

    private static final DateTimeFormatter ORDER_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Set<String> ACTIVE_ORDER_STATUSES = Set.of("CREATED", "PAID", "USING", "RETURNING");

    private final RentalOrderMapper rentalOrderMapper;
    private final CarInfoMapper carInfoMapper;
    private final RegionMapper regionMapper;
    private final SysUserMapper sysUserMapper;

    public UserOrderServiceImpl(RentalOrderMapper rentalOrderMapper,
                                CarInfoMapper carInfoMapper,
                                RegionMapper regionMapper,
                                SysUserMapper sysUserMapper) {
        this.rentalOrderMapper = rentalOrderMapper;
        this.carInfoMapper = carInfoMapper;
        this.regionMapper = regionMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public PageResult<UserOrderVO> page(UserOrderQueryDTO queryDTO) {
        Long userId = currentUserId();
        Page<RentalOrder> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<RentalOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentalOrder::getUserId, userId)
                .like(StringUtils.hasText(queryDTO.getOrderNo()), RentalOrder::getOrderNo, queryDTO.getOrderNo())
                .eq(StringUtils.hasText(queryDTO.getOrderStatus()), RentalOrder::getOrderStatus, queryDTO.getOrderStatus())
                .orderByDesc(RentalOrder::getCreateTime);
        Page<RentalOrder> result = rentalOrderMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), enrich(result.getRecords()));
    }

    @Override
    public UserOrderVO detail(Long id) {
        RentalOrder order = getCurrentUserOrder(id);
        return enrich(order);
    }

    @Override
    public void create(UserOrderCreateDTO createDTO) {
        Long userId = currentUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "\u5f53\u524d\u7528\u6237\u4e0d\u5b58\u5728");
        }
        CarInfo car = carInfoMapper.selectById(createDTO.getCarId());
        if (car == null) {
            throw new BusinessException(404, "\u8f66\u8f86\u4e0d\u5b58\u5728");
        }
        if (!"DEPLOYED".equals(car.getStatus()) || car.getCurrentRegionId() == null) {
            throw new BusinessException(400, "\u5f53\u524d\u8f66\u8f86\u4e0d\u53ef\u4e0b\u5355");
        }
        ensureCarOrderable(car.getId(), null);
        if (createDTO.getReturnRegionId() != null && regionMapper.selectById(createDTO.getReturnRegionId()) == null) {
            throw new BusinessException(400, "\u5f52\u8fd8\u533a\u57df\u4e0d\u5b58\u5728");
        }
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime expectedReturnTime = DateTimeUtil.parseDateTime(createDTO.getExpectedReturnTime(), "\u9884\u8ba1\u5f52\u8fd8\u65f6\u95f4");
        if (!expectedReturnTime.isAfter(startTime)) {
            throw new BusinessException(400, "\u9884\u8ba1\u5f52\u8fd8\u65f6\u95f4\u5fc5\u987b\u665a\u4e8e\u5f53\u524d\u65f6\u95f4");
        }

        RentalOrder order = new RentalOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setCarId(car.getId());
        order.setPickupRegionId(car.getCurrentRegionId());
        order.setReturnRegionId(createDTO.getReturnRegionId());
        order.setOrderStatus("CREATED");
        order.setOrderAmount(calculateOrderAmount(car.getDailyRent(), startTime, expectedReturnTime));
        order.setDepositAmount(car.getDeposit());
        order.setStartTime(startTime);
        order.setExpectedReturnTime(expectedReturnTime);
        order.setPaymentStatus("UNPAID");
        order.setRemark(createDTO.getRemark());
        rentalOrderMapper.insert(order);

        car.setStatus("RENTING");
        carInfoMapper.updateById(car);
    }

    @Override
    public void mockPay(Long id) {
        RentalOrder order = getCurrentUserOrder(id);
        if (!"CREATED".equals(order.getOrderStatus()) || !"UNPAID".equals(order.getPaymentStatus())) {
            throw new BusinessException(400, "\u5f53\u524d\u8ba2\u5355\u4e0d\u652f\u6301\u6a21\u62df\u652f\u4ed8");
        }
        CarInfo car = carInfoMapper.selectById(order.getCarId());
        if (car == null) {
            throw new BusinessException(404, "\u8f66\u8f86\u4e0d\u5b58\u5728");
        }
        if (!Set.of("DEPLOYED", "RENTING").contains(car.getStatus())) {
            throw new BusinessException(400, "\u5f53\u524d\u8f66\u8f86\u72b6\u6001\u4e0d\u5141\u8bb8\u652f\u4ed8");
        }
        ensureCarOrderable(order.getCarId(), order.getId());

        order.setPaymentStatus("PAID");
        order.setOrderStatus("USING");
        rentalOrderMapper.updateById(order);

        car.setStatus("RENTING");
        carInfoMapper.updateById(car);
    }

    @Override
    public UserOrderFormOptionsVO formOptions() {
        List<Long> reservedCarIds = rentalOrderMapper.selectList(new LambdaQueryWrapper<RentalOrder>()
                        .select(RentalOrder::getCarId)
                        .in(RentalOrder::getOrderStatus, ACTIVE_ORDER_STATUSES))
                .stream()
                .map(RentalOrder::getCarId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        LambdaQueryWrapper<CarInfo> carWrapper = new LambdaQueryWrapper<CarInfo>()
                .eq(CarInfo::getStatus, "DEPLOYED")
                .isNotNull(CarInfo::getCurrentRegionId)
                .orderByAsc(CarInfo::getCarCode);
        if (!reservedCarIds.isEmpty()) {
            carWrapper.notIn(CarInfo::getId, reservedCarIds);
        }

        List<CarSimpleOptionVO> cars = carInfoMapper.selectList(carWrapper)
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
        return new UserOrderFormOptionsVO(cars, regions);
    }

    private Long currentUserId() {
        if (UserContext.get() == null) {
            throw new BusinessException(401, "\u5f53\u524d\u7528\u6237\u672a\u767b\u5f55");
        }
        return UserContext.get().getUserId();
    }

    private RentalOrder getCurrentUserOrder(Long id) {
        RentalOrder order = rentalOrderMapper.selectById(id);
        if (order == null || !Objects.equals(order.getUserId(), currentUserId())) {
            throw new BusinessException(404, "\u8ba2\u5355\u4e0d\u5b58\u5728");
        }
        return order;
    }

    private void ensureCarOrderable(Long carId, Long excludeOrderId) {
        LambdaQueryWrapper<RentalOrder> wrapper = new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getCarId, carId)
                .in(RentalOrder::getOrderStatus, ACTIVE_ORDER_STATUSES);
        if (excludeOrderId != null) {
            wrapper.ne(RentalOrder::getId, excludeOrderId);
        }
        Long activeOrderCount = rentalOrderMapper.selectCount(wrapper);
        if (activeOrderCount != null && activeOrderCount > 0) {
            throw new BusinessException(409, "\u5f53\u524d\u8f66\u8f86\u5df2\u6709\u5f85\u5904\u7406\u8ba2\u5355");
        }
    }

    private List<UserOrderVO> enrich(List<RentalOrder> orders) {
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
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
        return orders.stream().map(item -> toVO(item, carMap, regionMap)).collect(Collectors.toList());
    }

    private UserOrderVO enrich(RentalOrder order) {
        Map<Long, CarInfo> carMap = Collections.emptyMap();
        CarInfo car = carInfoMapper.selectById(order.getCarId());
        if (car != null) {
            carMap = Map.of(car.getId(), car);
        }
        Map<Long, Region> regionMap = Collections.emptyMap();
        List<Long> regionIds = java.util.stream.Stream.of(order.getPickupRegionId(), order.getReturnRegionId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!regionIds.isEmpty()) {
            regionMap = regionMapper.selectBatchIds(regionIds).stream().collect(Collectors.toMap(Region::getId, item -> item));
        }
        return toVO(order, carMap, regionMap);
    }

    private UserOrderVO toVO(RentalOrder order, Map<Long, CarInfo> carMap, Map<Long, Region> regionMap) {
        CarInfo car = carMap.get(order.getCarId());
        Region pickup = regionMap.get(order.getPickupRegionId());
        Region returned = regionMap.get(order.getReturnRegionId());
        return UserOrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .carId(order.getCarId())
                .carCode(car == null ? null : car.getCarCode())
                .licensePlate(car == null ? null : car.getLicensePlate())
                .brand(car == null ? null : car.getBrand())
                .model(car == null ? null : car.getModel())
                .pickupRegionId(order.getPickupRegionId())
                .pickupRegionName(pickup == null ? null : pickup.getRegionName())
                .returnRegionId(order.getReturnRegionId())
                .returnRegionName(returned == null ? null : returned.getRegionName())
                .orderAmount(order.getOrderAmount())
                .depositAmount(order.getDepositAmount())
                .startTime(order.getStartTime())
                .expectedReturnTime(order.getExpectedReturnTime())
                .actualReturnTime(order.getActualReturnTime())
                .remark(order.getRemark())
                .createTime(order.getCreateTime())
                .build();
    }

    private String generateOrderNo() {
        return "UORD" + LocalDateTime.now().format(ORDER_NO_TIME_FORMATTER) + String.format("%03d", new Random().nextInt(1000));
    }

    private BigDecimal calculateOrderAmount(BigDecimal dailyRent, LocalDateTime startTime, LocalDateTime expectedReturnTime) {
        long hours = java.time.Duration.between(startTime, expectedReturnTime).toHours();
        long days = Math.max(1, (long) Math.ceil(hours / 24.0));
        return dailyRent.multiply(BigDecimal.valueOf(days));
    }
}
