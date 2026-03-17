package com.cartaxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cartaxi.common.context.UserContext;
import com.cartaxi.common.exception.BusinessException;
import com.cartaxi.dto.user.returnrecord.UserReturnCreateDTO;
import com.cartaxi.dto.user.returnrecord.UserReturnQueryDTO;
import com.cartaxi.entity.CarInfo;
import com.cartaxi.entity.Region;
import com.cartaxi.entity.RentalOrder;
import com.cartaxi.entity.ReturnRecord;
import com.cartaxi.entity.SysUser;
import com.cartaxi.mapper.CarInfoMapper;
import com.cartaxi.mapper.RegionMapper;
import com.cartaxi.mapper.RentalOrderMapper;
import com.cartaxi.mapper.ReturnRecordMapper;
import com.cartaxi.mapper.SysUserMapper;
import com.cartaxi.service.UserReturnService;
import com.cartaxi.vo.admin.region.RegionOptionVO;
import com.cartaxi.vo.common.PageResult;
import com.cartaxi.vo.user.returnrecord.UserReturnFormOptionsVO;
import com.cartaxi.vo.user.returnrecord.UserReturnVO;
import com.cartaxi.vo.user.returnrecord.UserReturnableOrderVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserReturnServiceImpl implements UserReturnService {

    private static final Set<String> AVAILABLE_ORDER_STATUS = Set.of("USING", "RETURNING");

    private final ReturnRecordMapper returnRecordMapper;
    private final RentalOrderMapper rentalOrderMapper;
    private final CarInfoMapper carInfoMapper;
    private final RegionMapper regionMapper;
    private final SysUserMapper sysUserMapper;

    public UserReturnServiceImpl(ReturnRecordMapper returnRecordMapper,
                                 RentalOrderMapper rentalOrderMapper,
                                 CarInfoMapper carInfoMapper,
                                 RegionMapper regionMapper,
                                 SysUserMapper sysUserMapper) {
        this.returnRecordMapper = returnRecordMapper;
        this.rentalOrderMapper = rentalOrderMapper;
        this.carInfoMapper = carInfoMapper;
        this.regionMapper = regionMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public PageResult<UserReturnVO> page(UserReturnQueryDTO queryDTO) {
        Long userId = currentUserId();
        LambdaQueryWrapper<ReturnRecord> wrapper = new LambdaQueryWrapper<ReturnRecord>()
                .eq(ReturnRecord::getUserId, userId)
                .eq(StringUtils.hasText(queryDTO.getStatus()), ReturnRecord::getStatus, queryDTO.getStatus())
                .orderByDesc(ReturnRecord::getReturnTime)
                .orderByDesc(ReturnRecord::getCreateTime);

        if (StringUtils.hasText(queryDTO.getOrderNo())) {
            List<Long> orderIds = rentalOrderMapper.selectList(new LambdaQueryWrapper<RentalOrder>()
                            .eq(RentalOrder::getUserId, userId)
                            .like(RentalOrder::getOrderNo, queryDTO.getOrderNo()))
                    .stream().map(RentalOrder::getId).toList();
            if (orderIds.isEmpty()) {
                return PageResult.empty(queryDTO.getPageNum(), queryDTO.getPageSize());
            }
            wrapper.in(ReturnRecord::getRentalOrderId, orderIds);
        }

        Page<ReturnRecord> page = returnRecordMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
                wrapper
        );
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), enrich(page.getRecords()));
    }

    @Override
    public UserReturnVO detail(Long id) {
        ReturnRecord record = getOwnedRecordOrThrow(id);
        return enrich(record);
    }

    @Override
    public UserReturnFormOptionsVO formOptions() {
        Long userId = currentUserId();
        List<RentalOrder> ownedOrders = rentalOrderMapper.selectList(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getUserId, userId)
                .in(RentalOrder::getOrderStatus, AVAILABLE_ORDER_STATUS)
                .orderByDesc(RentalOrder::getCreateTime));
        if (ownedOrders.isEmpty()) {
            return new UserReturnFormOptionsVO(Collections.emptyList(), activeRegions());
        }

        List<Long> orderIds = ownedOrders.stream().map(RentalOrder::getId).toList();
        Set<Long> returnedOrderIds = returnRecordMapper.selectList(new LambdaQueryWrapper<ReturnRecord>()
                        .in(ReturnRecord::getRentalOrderId, orderIds))
                .stream().map(ReturnRecord::getRentalOrderId).collect(Collectors.toSet());

        Map<Long, CarInfo> carMap = carInfoMapper.selectBatchIds(ownedOrders.stream().map(RentalOrder::getCarId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(CarInfo::getId, item -> item));
        Map<Long, Region> regionMap = regionMapper.selectBatchIds(ownedOrders.stream()
                        .flatMap(item -> java.util.stream.Stream.of(item.getPickupRegionId(), item.getReturnRegionId()))
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList())
                .stream().collect(Collectors.toMap(Region::getId, item -> item));

        List<UserReturnableOrderVO> options = ownedOrders.stream()
                .filter(order -> !returnedOrderIds.contains(order.getId()))
                .map(order -> {
                    CarInfo car = carMap.get(order.getCarId());
                    Region pickupRegion = regionMap.get(order.getPickupRegionId());
                    Region returnRegion = regionMap.get(order.getReturnRegionId());
                    return UserReturnableOrderVO.builder()
                            .id(order.getId())
                            .orderNo(order.getOrderNo())
                            .carId(order.getCarId())
                            .carCode(car == null ? null : car.getCarCode())
                            .licensePlate(car == null ? null : car.getLicensePlate())
                            .brand(car == null ? null : car.getBrand())
                            .model(car == null ? null : car.getModel())
                            .pickupRegionId(order.getPickupRegionId())
                            .pickupRegionName(pickupRegion == null ? null : pickupRegion.getRegionName())
                            .returnRegionId(order.getReturnRegionId())
                            .returnRegionName(returnRegion == null ? null : returnRegion.getRegionName())
                            .orderStatus(order.getOrderStatus())
                            .startTime(order.getStartTime())
                            .expectedReturnTime(order.getExpectedReturnTime())
                            .remark(order.getRemark())
                            .build();
                })
                .toList();
        return new UserReturnFormOptionsVO(options, activeRegions());
    }

    @Override
    public void create(UserReturnCreateDTO createDTO) {
        Long userId = currentUserId();
        ensureCurrentUserExists(userId);
        RentalOrder order = getOwnedOrderOrThrow(createDTO.getRentalOrderId(), userId);
        if (!AVAILABLE_ORDER_STATUS.contains(order.getOrderStatus())) {
            throw new BusinessException(400, "当前订单状态不允许发起归还申请");
        }
        ReturnRecord existed = returnRecordMapper.selectOne(new LambdaQueryWrapper<ReturnRecord>()
                .eq(ReturnRecord::getRentalOrderId, order.getId()));
        if (existed != null) {
            throw new BusinessException(409, "该订单已提交过归还申请");
        }

        Region region = regionMapper.selectById(createDTO.getReturnRegionId());
        if (region == null || !Objects.equals(region.getStatus(), 1)) {
            throw new BusinessException(400, "归还区域不存在或不可用");
        }

        CarInfo car = carInfoMapper.selectById(order.getCarId());
        if (car == null) {
            throw new BusinessException(404, "车辆不存在");
        }

        ReturnRecord record = new ReturnRecord();
        record.setRentalOrderId(order.getId());
        record.setCarId(order.getCarId());
        record.setUserId(userId);
        record.setReturnRegionId(createDTO.getReturnRegionId());
        record.setStatus("PENDING");
        record.setVehicleCondition(createDTO.getVehicleCondition());
        record.setDamageCost(BigDecimal.ZERO);
        record.setLateFee(BigDecimal.ZERO);
        record.setFinalAmount(BigDecimal.ZERO);
        record.setReturnTime(LocalDateTime.now());
        record.setRemark(createDTO.getRemark());
        returnRecordMapper.insert(record);

        order.setOrderStatus("RETURNING");
        order.setReturnRegionId(createDTO.getReturnRegionId());
        order.setActualReturnTime(record.getReturnTime());
        rentalOrderMapper.updateById(order);

        car.setStatus("RETURN_PENDING");
        carInfoMapper.updateById(car);
    }

    private Long currentUserId() {
        if (UserContext.get() == null || UserContext.get().getUserId() == null) {
            throw new BusinessException(401, "当前用户未登录");
        }
        return UserContext.get().getUserId();
    }

    private void ensureCurrentUserExists(Long userId) {
        if (sysUserMapper.selectById(userId) == null) {
            throw new BusinessException(404, "用户不存在");
        }
    }

    private RentalOrder getOwnedOrderOrThrow(Long orderId, Long userId) {
        RentalOrder order = rentalOrderMapper.selectOne(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getId, orderId)
                .eq(RentalOrder::getUserId, userId));
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return order;
    }

    private ReturnRecord getOwnedRecordOrThrow(Long id) {
        ReturnRecord record = returnRecordMapper.selectById(id);
        if (record == null || !Objects.equals(record.getUserId(), currentUserId())) {
            throw new BusinessException(404, "归还记录不存在");
        }
        return record;
    }

    private List<RegionOptionVO> activeRegions() {
        return regionMapper.selectList(new LambdaQueryWrapper<Region>()
                        .eq(Region::getStatus, 1)
                        .orderByAsc(Region::getRegionCode))
                .stream()
                .map(item -> new RegionOptionVO(item.getId(), item.getRegionName(), item.getRegionCode()))
                .toList();
    }

    private List<UserReturnVO> enrich(List<ReturnRecord> records) {
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, RentalOrder> orderMap = rentalOrderMapper.selectBatchIds(records.stream().map(ReturnRecord::getRentalOrderId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(RentalOrder::getId, item -> item));
        Map<Long, CarInfo> carMap = carInfoMapper.selectBatchIds(records.stream().map(ReturnRecord::getCarId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(CarInfo::getId, item -> item));
        Map<Long, Region> regionMap = regionMapper.selectBatchIds(records.stream().map(ReturnRecord::getReturnRegionId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Region::getId, item -> item));
        return records.stream().map(item -> toVO(item, orderMap, carMap, regionMap)).toList();
    }

    private UserReturnVO enrich(ReturnRecord record) {
        RentalOrder order = rentalOrderMapper.selectById(record.getRentalOrderId());
        CarInfo car = carInfoMapper.selectById(record.getCarId());
        Region region = regionMapper.selectById(record.getReturnRegionId());
        return toVO(
                record,
                order == null ? Collections.emptyMap() : Map.of(order.getId(), order),
                car == null ? Collections.emptyMap() : Map.of(car.getId(), car),
                region == null ? Collections.emptyMap() : Map.of(region.getId(), region)
        );
    }

    private UserReturnVO toVO(ReturnRecord record,
                              Map<Long, RentalOrder> orderMap,
                              Map<Long, CarInfo> carMap,
                              Map<Long, Region> regionMap) {
        RentalOrder order = orderMap.get(record.getRentalOrderId());
        CarInfo car = carMap.get(record.getCarId());
        Region region = regionMap.get(record.getReturnRegionId());
        return UserReturnVO.builder()
                .id(record.getId())
                .rentalOrderId(record.getRentalOrderId())
                .orderNo(order == null ? null : order.getOrderNo())
                .carId(record.getCarId())
                .carCode(car == null ? null : car.getCarCode())
                .licensePlate(car == null ? null : car.getLicensePlate())
                .brand(car == null ? null : car.getBrand())
                .model(car == null ? null : car.getModel())
                .returnRegionId(record.getReturnRegionId())
                .returnRegionName(region == null ? null : region.getRegionName())
                .status(record.getStatus())
                .vehicleCondition(record.getVehicleCondition())
                .damageCost(record.getDamageCost())
                .lateFee(record.getLateFee())
                .finalAmount(record.getFinalAmount())
                .returnTime(record.getReturnTime())
                .createTime(record.getCreateTime())
                .remark(record.getRemark())
                .build();
    }
}
