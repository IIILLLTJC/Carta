package com.cartaxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cartaxi.common.exception.BusinessException;
import com.cartaxi.common.util.DateTimeUtil;
import com.cartaxi.dto.admin.recovery.CarRecoveryRecordQueryDTO;
import com.cartaxi.dto.admin.recovery.CarRecoveryRecordSaveDTO;
import com.cartaxi.entity.CarInfo;
import com.cartaxi.entity.CarRecoveryRecord;
import com.cartaxi.entity.Region;
import com.cartaxi.entity.SysAdmin;
import com.cartaxi.mapper.CarInfoMapper;
import com.cartaxi.mapper.CarRecoveryRecordMapper;
import com.cartaxi.mapper.RegionMapper;
import com.cartaxi.mapper.SysAdminMapper;
import com.cartaxi.service.AdminCarRecoveryService;
import com.cartaxi.vo.admin.deploy.AdminSimpleOptionVO;
import com.cartaxi.vo.admin.deploy.CarSimpleOptionVO;
import com.cartaxi.vo.admin.recovery.CarRecoveryFormOptionsVO;
import com.cartaxi.vo.admin.recovery.CarRecoveryRecordVO;
import com.cartaxi.vo.admin.region.RegionOptionVO;
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
public class AdminCarRecoveryServiceImpl implements AdminCarRecoveryService {

    private static final Set<String> ALLOWED_STATUS = Set.of("PENDING", "RECOVERED", "MAINTENANCE", "CANCELLED");

    private final CarRecoveryRecordMapper recoveryRecordMapper;
    private final CarInfoMapper carInfoMapper;
    private final RegionMapper regionMapper;
    private final SysAdminMapper sysAdminMapper;

    public AdminCarRecoveryServiceImpl(CarRecoveryRecordMapper recoveryRecordMapper,
                                       CarInfoMapper carInfoMapper,
                                       RegionMapper regionMapper,
                                       SysAdminMapper sysAdminMapper) {
        this.recoveryRecordMapper = recoveryRecordMapper;
        this.carInfoMapper = carInfoMapper;
        this.regionMapper = regionMapper;
        this.sysAdminMapper = sysAdminMapper;
    }

    @Override
    public PageResult<CarRecoveryRecordVO> page(CarRecoveryRecordQueryDTO queryDTO) {
        List<Long> carIds = findMatchedCarIds(queryDTO.getCarKeyword());
        if (StringUtils.hasText(queryDTO.getCarKeyword()) && carIds.isEmpty()) {
            return PageResult.empty(queryDTO.getPageNum(), queryDTO.getPageSize());
        }
        Page<CarRecoveryRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<CarRecoveryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(carIds != null && !carIds.isEmpty(), CarRecoveryRecord::getCarId, carIds)
                .eq(queryDTO.getRegionId() != null, CarRecoveryRecord::getRegionId, queryDTO.getRegionId())
                .eq(StringUtils.hasText(queryDTO.getStatus()), CarRecoveryRecord::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getOperatorAdminId() != null, CarRecoveryRecord::getOperatorAdminId, queryDTO.getOperatorAdminId())
                .orderByDesc(CarRecoveryRecord::getRecoveryTime)
                .orderByDesc(CarRecoveryRecord::getCreateTime);
        Page<CarRecoveryRecord> result = recoveryRecordMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), enrich(result.getRecords()));
    }

    @Override
    public CarRecoveryRecordVO detail(Long id) {
        return enrich(getRecoveryRecordOrThrow(id));
    }

    @Override
    public void create(CarRecoveryRecordSaveDTO saveDTO) {
        validateSaveDTO(saveDTO);
        CarRecoveryRecord record = new CarRecoveryRecord();
        BeanUtils.copyProperties(saveDTO, record, "recoveryTime");
        record.setRecoveryTime(DateTimeUtil.parseDateTime(saveDTO.getRecoveryTime(), "回收时间"));
        recoveryRecordMapper.insert(record);
        syncCarStatus(record);
    }

    @Override
    public void update(Long id, CarRecoveryRecordSaveDTO saveDTO) {
        CarRecoveryRecord record = getRecoveryRecordOrThrow(id);
        validateSaveDTO(saveDTO);
        BeanUtils.copyProperties(saveDTO, record, "recoveryTime");
        record.setRecoveryTime(DateTimeUtil.parseDateTime(saveDTO.getRecoveryTime(), "回收时间"));
        recoveryRecordMapper.updateById(record);
        syncCarStatus(record);
    }

    @Override
    public void delete(Long id) {
        getRecoveryRecordOrThrow(id);
        recoveryRecordMapper.deleteById(id);
    }

    @Override
    public void changeStatus(Long id, String status) {
        validateStatus(status);
        CarRecoveryRecord record = getRecoveryRecordOrThrow(id);
        record.setStatus(status);
        recoveryRecordMapper.updateById(record);
        syncCarStatus(record);
    }

    @Override
    public CarRecoveryFormOptionsVO formOptions() {
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
        List<AdminSimpleOptionVO> admins = sysAdminMapper.selectList(new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getStatus, 1)
                        .orderByAsc(SysAdmin::getUsername))
                .stream()
                .map(item -> new AdminSimpleOptionVO(item.getId(), item.getUsername(), item.getRealName()))
                .collect(Collectors.toList());
        return new CarRecoveryFormOptionsVO(cars, regions, admins);
    }

    private void validateSaveDTO(CarRecoveryRecordSaveDTO saveDTO) {
        validateStatus(saveDTO.getStatus());
        if (carInfoMapper.selectById(saveDTO.getCarId()) == null) {
            throw new BusinessException(400, "车辆不存在");
        }
        if (saveDTO.getRegionId() != null && regionMapper.selectById(saveDTO.getRegionId()) == null) {
            throw new BusinessException(400, "区域不存在");
        }
        if (sysAdminMapper.selectById(saveDTO.getOperatorAdminId()) == null) {
            throw new BusinessException(400, "操作管理员不存在");
        }
    }

    private void validateStatus(String status) {
        if (!StringUtils.hasText(status) || !ALLOWED_STATUS.contains(status)) {
            throw new BusinessException(400, "回收状态不合法");
        }
    }

    private CarRecoveryRecord getRecoveryRecordOrThrow(Long id) {
        CarRecoveryRecord record = recoveryRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(404, "回收记录不存在");
        }
        return record;
    }

    private void syncCarStatus(CarRecoveryRecord record) {
        CarInfo car = carInfoMapper.selectById(record.getCarId());
        if (car == null) {
            return;
        }
        switch (record.getStatus()) {
            case "PENDING" -> car.setStatus("RETURN_PENDING");
            case "RECOVERED" -> {
                car.setCurrentRegionId(null);
                car.setStatus("RECOVERED");
            }
            case "MAINTENANCE" -> {
                car.setCurrentRegionId(null);
                car.setStatus("MAINTENANCE");
            }
            case "CANCELLED" -> car.setStatus(car.getCurrentRegionId() == null ? "IDLE" : "DEPLOYED");
            default -> {
            }
        }
        carInfoMapper.updateById(car);
    }

    private List<CarRecoveryRecordVO> enrich(List<CarRecoveryRecord> records) {
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, CarInfo> carMap = carInfoMapper.selectBatchIds(records.stream().map(CarRecoveryRecord::getCarId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(CarInfo::getId, item -> item));
        Map<Long, Region> regionMap = regionMapper.selectBatchIds(records.stream().map(CarRecoveryRecord::getRegionId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Region::getId, item -> item));
        Map<Long, SysAdmin> adminMap = sysAdminMapper.selectBatchIds(records.stream().map(CarRecoveryRecord::getOperatorAdminId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(SysAdmin::getId, item -> item));
        return records.stream().map(item -> toVO(item, carMap, regionMap, adminMap)).collect(Collectors.toList());
    }

    private CarRecoveryRecordVO enrich(CarRecoveryRecord record) {
        Map<Long, CarInfo> carMap = new HashMap<>();
        CarInfo car = carInfoMapper.selectById(record.getCarId());
        if (car != null) {
            carMap.put(car.getId(), car);
        }
        Map<Long, Region> regionMap = new HashMap<>();
        if (record.getRegionId() != null) {
            Region region = regionMapper.selectById(record.getRegionId());
            if (region != null) {
                regionMap.put(region.getId(), region);
            }
        }
        Map<Long, SysAdmin> adminMap = new HashMap<>();
        SysAdmin admin = sysAdminMapper.selectById(record.getOperatorAdminId());
        if (admin != null) {
            adminMap.put(admin.getId(), admin);
        }
        return toVO(record, carMap, regionMap, adminMap);
    }

    private CarRecoveryRecordVO toVO(CarRecoveryRecord item,
                                     Map<Long, CarInfo> carMap,
                                     Map<Long, Region> regionMap,
                                     Map<Long, SysAdmin> adminMap) {
        CarInfo car = carMap.get(item.getCarId());
        Region region = regionMap.get(item.getRegionId());
        SysAdmin admin = adminMap.get(item.getOperatorAdminId());
        return CarRecoveryRecordVO.builder()
                .id(item.getId())
                .carId(item.getCarId())
                .carCode(car == null ? null : car.getCarCode())
                .licensePlate(car == null ? null : car.getLicensePlate())
                .brand(car == null ? null : car.getBrand())
                .model(car == null ? null : car.getModel())
                .regionId(item.getRegionId())
                .regionName(region == null ? null : region.getRegionName())
                .operatorAdminId(item.getOperatorAdminId())
                .operatorAdminName(admin == null ? null : (StringUtils.hasText(admin.getRealName()) ? admin.getRealName() : admin.getUsername()))
                .recoveryTime(item.getRecoveryTime())
                .recoveryReason(item.getRecoveryReason())
                .status(item.getStatus())
                .remark(item.getRemark())
                .createTime(item.getCreateTime())
                .build();
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