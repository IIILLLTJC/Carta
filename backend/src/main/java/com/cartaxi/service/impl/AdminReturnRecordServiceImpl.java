package com.cartaxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cartaxi.common.exception.BusinessException;
import com.cartaxi.common.util.DateTimeUtil;
import com.cartaxi.dto.admin.returnrecord.ReturnRecordQueryDTO;
import com.cartaxi.dto.admin.returnrecord.ReturnRecordSaveDTO;
import com.cartaxi.entity.CarInfo;
import com.cartaxi.entity.Region;
import com.cartaxi.entity.RentalOrder;
import com.cartaxi.entity.ReturnRecord;
import com.cartaxi.entity.SysAdmin;
import com.cartaxi.entity.SysUser;
import com.cartaxi.mapper.CarInfoMapper;
import com.cartaxi.mapper.RegionMapper;
import com.cartaxi.mapper.RentalOrderMapper;
import com.cartaxi.mapper.ReturnRecordMapper;
import com.cartaxi.mapper.SysAdminMapper;
import com.cartaxi.mapper.SysUserMapper;
import com.cartaxi.service.AdminReturnRecordService;
import com.cartaxi.vo.admin.deploy.AdminSimpleOptionVO;
import com.cartaxi.vo.admin.region.RegionOptionVO;
import com.cartaxi.vo.admin.returnrecord.ReturnOrderOptionVO;
import com.cartaxi.vo.admin.returnrecord.ReturnRecordFormOptionsVO;
import com.cartaxi.vo.admin.returnrecord.ReturnRecordVO;
import com.cartaxi.vo.common.PageResult;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminReturnRecordServiceImpl implements AdminReturnRecordService {

    private static final Set<String> ALLOWED_STATUS = Set.of("PENDING", "CONFIRMED", "SETTLED", "COMPLETED");

    private final ReturnRecordMapper returnRecordMapper;
    private final RentalOrderMapper rentalOrderMapper;
    private final CarInfoMapper carInfoMapper;
    private final RegionMapper regionMapper;
    private final SysUserMapper sysUserMapper;
    private final SysAdminMapper sysAdminMapper;

    public AdminReturnRecordServiceImpl(ReturnRecordMapper returnRecordMapper,
                                        RentalOrderMapper rentalOrderMapper,
                                        CarInfoMapper carInfoMapper,
                                        RegionMapper regionMapper,
                                        SysUserMapper sysUserMapper,
                                        SysAdminMapper sysAdminMapper) {
        this.returnRecordMapper = returnRecordMapper;
        this.rentalOrderMapper = rentalOrderMapper;
        this.carInfoMapper = carInfoMapper;
        this.regionMapper = regionMapper;
        this.sysUserMapper = sysUserMapper;
        this.sysAdminMapper = sysAdminMapper;
    }

    @Override
    public PageResult<ReturnRecordVO> page(ReturnRecordQueryDTO queryDTO) {
        List<Long> orderIds = findMatchedOrderIds(queryDTO.getOrderNo());
        if (StringUtils.hasText(queryDTO.getOrderNo()) && orderIds.isEmpty()) {
            return PageResult.empty(queryDTO.getPageNum(), queryDTO.getPageSize());
        }
        List<Long> carIds = findMatchedCarIds(queryDTO.getCarKeyword());
        if (StringUtils.hasText(queryDTO.getCarKeyword()) && carIds.isEmpty()) {
            return PageResult.empty(queryDTO.getPageNum(), queryDTO.getPageSize());
        }
        List<Long> userIds = findMatchedUserIds(queryDTO.getUserKeyword());
        if (StringUtils.hasText(queryDTO.getUserKeyword()) && userIds.isEmpty()) {
            return PageResult.empty(queryDTO.getPageNum(), queryDTO.getPageSize());
        }

        Page<ReturnRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<ReturnRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(orderIds != null && !orderIds.isEmpty(), ReturnRecord::getRentalOrderId, orderIds)
                .in(carIds != null && !carIds.isEmpty(), ReturnRecord::getCarId, carIds)
                .in(userIds != null && !userIds.isEmpty(), ReturnRecord::getUserId, userIds)
                .eq(queryDTO.getReturnRegionId() != null, ReturnRecord::getReturnRegionId, queryDTO.getReturnRegionId())
                .eq(StringUtils.hasText(queryDTO.getStatus()), ReturnRecord::getStatus, queryDTO.getStatus())
                .orderByDesc(ReturnRecord::getReturnTime)
                .orderByDesc(ReturnRecord::getCreateTime);
        Page<ReturnRecord> result = returnRecordMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), enrich(result.getRecords()));
    }

    @Override
    public ReturnRecordVO detail(Long id) {
        return enrich(getReturnRecordOrThrow(id));
    }

    @Override
    public void create(ReturnRecordSaveDTO saveDTO) {
        validateSaveDTO(saveDTO, null);
        RentalOrder order = getOrderOrThrow(saveDTO.getRentalOrderId());
        ReturnRecord record = new ReturnRecord();
        BeanUtils.copyProperties(saveDTO, record, "returnTime");
        record.setCarId(order.getCarId());
        record.setUserId(order.getUserId());
        record.setReturnTime(DateTimeUtil.parseDateTime(saveDTO.getReturnTime(), "归还时间"));
        returnRecordMapper.insert(record);
        syncOrderAndCar(record);
    }

    @Override
    public void update(Long id, ReturnRecordSaveDTO saveDTO) {
        ReturnRecord record = getReturnRecordOrThrow(id);
        validateSaveDTO(saveDTO, id);
        RentalOrder order = getOrderOrThrow(saveDTO.getRentalOrderId());
        BeanUtils.copyProperties(saveDTO, record, "returnTime");
        record.setCarId(order.getCarId());
        record.setUserId(order.getUserId());
        record.setReturnTime(DateTimeUtil.parseDateTime(saveDTO.getReturnTime(), "归还时间"));
        returnRecordMapper.updateById(record);
        syncOrderAndCar(record);
    }

    @Override
    public void delete(Long id) {
        getReturnRecordOrThrow(id);
        returnRecordMapper.deleteById(id);
    }

    @Override
    public void changeStatus(Long id, String status) {
        validateStatus(status);
        ReturnRecord record = getReturnRecordOrThrow(id);
        record.setStatus(status);
        returnRecordMapper.updateById(record);
        syncOrderAndCar(record);
    }

    @Override
    public ReturnRecordFormOptionsVO formOptions() {
        List<ReturnOrderOptionVO> orders = rentalOrderMapper.selectList(new LambdaQueryWrapper<RentalOrder>()
                        .orderByDesc(RentalOrder::getCreateTime))
                .stream()
                .map(order -> {
                    SysUser user = sysUserMapper.selectById(order.getUserId());
                    CarInfo car = carInfoMapper.selectById(order.getCarId());
                    String userName = user == null ? "未知用户" : (StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername());
                    String carName = car == null ? "未知车辆" : car.getCarCode() + " / " + car.getLicensePlate();
                    return new ReturnOrderOptionVO(order.getId(), order.getOrderNo(), order.getOrderNo() + " / " + userName + " / " + carName);
                })
                .collect(Collectors.toList());
        List<RegionOptionVO> regions = regionMapper.selectList(new LambdaQueryWrapper<Region>()
                        .eq(Region::getStatus, 1)
                        .orderByAsc(Region::getRegionCode))
                .stream()
                .map(item -> new RegionOptionVO(item.getId(), item.getRegionName(), item.getRegionCode()))
                .collect(Collectors.toList());
        List<AdminSimpleOptionVO> admins = sysAdminMapper.selectList(new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getStatus, 1)
                        .orderByAsc(SysAdmin::getUsername))
                .stream()
                .map(item -> new AdminSimpleOptionVO(item.getId(), item.getUsername(), item.getRealName()))
                .collect(Collectors.toList());
        return new ReturnRecordFormOptionsVO(orders, regions, admins);
    }

    private void validateSaveDTO(ReturnRecordSaveDTO saveDTO, Long currentId) {
        validateStatus(saveDTO.getStatus());
        RentalOrder order = getOrderOrThrow(saveDTO.getRentalOrderId());
        if (regionMapper.selectById(saveDTO.getReturnRegionId()) == null) {
            throw new BusinessException(400, "归还区域不存在");
        }
        if (saveDTO.getProcessedBy() != null && sysAdminMapper.selectById(saveDTO.getProcessedBy()) == null) {
            throw new BusinessException(400, "处理管理员不存在");
        }
        ReturnRecord existed = returnRecordMapper.selectOne(new LambdaQueryWrapper<ReturnRecord>()
                .eq(ReturnRecord::getRentalOrderId, saveDTO.getRentalOrderId()));
        if (existed != null && !Objects.equals(existed.getId(), currentId)) {
            throw new BusinessException(409, "该订单已存在归还记录");
        }
        if (order.getStartTime() != null) {
            if (DateTimeUtil.parseDateTime(saveDTO.getReturnTime(), "归还时间").isBefore(order.getStartTime())) {
                throw new BusinessException(400, "归还时间不能早于订单开始时间");
            }
        }
    }

    private void validateStatus(String status) {
        if (!StringUtils.hasText(status) || !ALLOWED_STATUS.contains(status)) {
            throw new BusinessException(400, "归还状态不合法");
        }
    }

    private ReturnRecord getReturnRecordOrThrow(Long id) {
        ReturnRecord record = returnRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(404, "归还记录不存在");
        }
        return record;
    }

    private RentalOrder getOrderOrThrow(Long id) {
        RentalOrder order = rentalOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return order;
    }

    private void syncOrderAndCar(ReturnRecord record) {
        RentalOrder order = rentalOrderMapper.selectById(record.getRentalOrderId());
        CarInfo car = carInfoMapper.selectById(record.getCarId());
        if (order == null || car == null) {
            return;
        }

        order.setReturnRegionId(record.getReturnRegionId());
        order.setActualReturnTime(record.getReturnTime());
        switch (record.getStatus()) {
            case "PENDING", "CONFIRMED" -> {
                order.setOrderStatus("RETURNING");
                car.setStatus("RETURN_PENDING");
            }
            case "SETTLED", "COMPLETED" -> {
                order.setOrderStatus("COMPLETED");
                car.setCurrentRegionId(record.getReturnRegionId());
                car.setStatus("DEPLOYED");
            }
            default -> {
            }
        }
        rentalOrderMapper.updateById(order);
        carInfoMapper.updateById(car);
    }

    private List<ReturnRecordVO> enrich(List<ReturnRecord> records) {
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, RentalOrder> orderMap = rentalOrderMapper.selectBatchIds(records.stream().map(ReturnRecord::getRentalOrderId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(RentalOrder::getId, item -> item));
        Map<Long, CarInfo> carMap = carInfoMapper.selectBatchIds(records.stream().map(ReturnRecord::getCarId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(CarInfo::getId, item -> item));
        Map<Long, SysUser> userMap = sysUserMapper.selectBatchIds(records.stream().map(ReturnRecord::getUserId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(SysUser::getId, item -> item));
        Map<Long, Region> regionMap = regionMapper.selectBatchIds(records.stream().map(ReturnRecord::getReturnRegionId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Region::getId, item -> item));
        Map<Long, SysAdmin> adminMap = sysAdminMapper.selectBatchIds(records.stream().map(ReturnRecord::getProcessedBy).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(SysAdmin::getId, item -> item));
        return records.stream().map(item -> toVO(item, orderMap, carMap, userMap, regionMap, adminMap)).collect(Collectors.toList());
    }

    private ReturnRecordVO enrich(ReturnRecord record) {
        Map<Long, RentalOrder> orderMap = new HashMap<>();
        RentalOrder order = rentalOrderMapper.selectById(record.getRentalOrderId());
        if (order != null) {
            orderMap.put(order.getId(), order);
        }
        Map<Long, CarInfo> carMap = new HashMap<>();
        CarInfo car = carInfoMapper.selectById(record.getCarId());
        if (car != null) {
            carMap.put(car.getId(), car);
        }
        Map<Long, SysUser> userMap = new HashMap<>();
        SysUser user = sysUserMapper.selectById(record.getUserId());
        if (user != null) {
            userMap.put(user.getId(), user);
        }
        Map<Long, Region> regionMap = new HashMap<>();
        Region region = regionMapper.selectById(record.getReturnRegionId());
        if (region != null) {
            regionMap.put(region.getId(), region);
        }
        Map<Long, SysAdmin> adminMap = new HashMap<>();
        if (record.getProcessedBy() != null) {
            SysAdmin admin = sysAdminMapper.selectById(record.getProcessedBy());
            if (admin != null) {
                adminMap.put(admin.getId(), admin);
            }
        }
        return toVO(record, orderMap, carMap, userMap, regionMap, adminMap);
    }

    private ReturnRecordVO toVO(ReturnRecord record,
                                Map<Long, RentalOrder> orderMap,
                                Map<Long, CarInfo> carMap,
                                Map<Long, SysUser> userMap,
                                Map<Long, Region> regionMap,
                                Map<Long, SysAdmin> adminMap) {
        RentalOrder order = orderMap.get(record.getRentalOrderId());
        CarInfo car = carMap.get(record.getCarId());
        SysUser user = userMap.get(record.getUserId());
        Region region = regionMap.get(record.getReturnRegionId());
        SysAdmin admin = adminMap.get(record.getProcessedBy());
        return ReturnRecordVO.builder()
                .id(record.getId())
                .rentalOrderId(record.getRentalOrderId())
                .orderNo(order == null ? null : order.getOrderNo())
                .carId(record.getCarId())
                .carCode(car == null ? null : car.getCarCode())
                .licensePlate(car == null ? null : car.getLicensePlate())
                .brand(car == null ? null : car.getBrand())
                .model(car == null ? null : car.getModel())
                .userId(record.getUserId())
                .username(user == null ? null : user.getUsername())
                .realName(user == null ? null : user.getRealName())
                .phone(user == null ? null : user.getPhone())
                .returnRegionId(record.getReturnRegionId())
                .returnRegionName(region == null ? null : region.getRegionName())
                .status(record.getStatus())
                .vehicleCondition(record.getVehicleCondition())
                .damageCost(record.getDamageCost())
                .lateFee(record.getLateFee())
                .finalAmount(record.getFinalAmount())
                .processedBy(record.getProcessedBy())
                .processedByName(admin == null ? null : (StringUtils.hasText(admin.getRealName()) ? admin.getRealName() : admin.getUsername()))
                .returnTime(record.getReturnTime())
                .createTime(record.getCreateTime())
                .updateTime(record.getUpdateTime())
                .remark(record.getRemark())
                .build();
    }

    private List<Long> findMatchedOrderIds(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return null;
        }
        return rentalOrderMapper.selectList(new LambdaQueryWrapper<RentalOrder>().like(RentalOrder::getOrderNo, orderNo))
                .stream().map(RentalOrder::getId).toList();
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
}