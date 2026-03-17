package com.cartaxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cartaxi.common.exception.BusinessException;
import com.cartaxi.dto.admin.car.CarInfoQueryDTO;
import com.cartaxi.dto.admin.car.CarInfoSaveDTO;
import com.cartaxi.entity.CarInfo;
import com.cartaxi.entity.Region;
import com.cartaxi.mapper.CarInfoMapper;
import com.cartaxi.mapper.RegionMapper;
import com.cartaxi.service.AdminCarInfoService;
import com.cartaxi.vo.admin.car.CarInfoVO;
import com.cartaxi.vo.common.PageResult;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminCarInfoServiceImpl implements AdminCarInfoService {

    private static final Set<String> ALLOWED_STATUS = Set.of(
            "IDLE", "DEPLOYED", "RENTING", "RETURN_PENDING", "RECOVERED", "MAINTENANCE"
    );

    private final CarInfoMapper carInfoMapper;
    private final RegionMapper regionMapper;

    public AdminCarInfoServiceImpl(CarInfoMapper carInfoMapper, RegionMapper regionMapper) {
        this.carInfoMapper = carInfoMapper;
        this.regionMapper = regionMapper;
    }

    @Override
    public PageResult<CarInfoVO> page(CarInfoQueryDTO queryDTO) {
        Page<CarInfo> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<CarInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getCarCode()), CarInfo::getCarCode, queryDTO.getCarCode())
                .like(StringUtils.hasText(queryDTO.getLicensePlate()), CarInfo::getLicensePlate, queryDTO.getLicensePlate())
                .like(StringUtils.hasText(queryDTO.getBrand()), CarInfo::getBrand, queryDTO.getBrand())
                .like(StringUtils.hasText(queryDTO.getModel()), CarInfo::getModel, queryDTO.getModel())
                .eq(StringUtils.hasText(queryDTO.getStatus()), CarInfo::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getCurrentRegionId() != null, CarInfo::getCurrentRegionId, queryDTO.getCurrentRegionId())
                .orderByDesc(CarInfo::getCreateTime);
        Page<CarInfo> result = carInfoMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), convertToVO(result.getRecords()));
    }

    @Override
    public CarInfoVO detail(Long id) {
        return convertToVO(getCarInfoOrThrow(id));
    }

    @Override
    public void create(CarInfoSaveDTO saveDTO) {
        validateSaveDTO(saveDTO);
        ensureUnique(null, saveDTO.getCarCode(), saveDTO.getLicensePlate());
        CarInfo carInfo = new CarInfo();
        BeanUtils.copyProperties(saveDTO, carInfo);
        normalizeNumericFields(carInfo);
        carInfoMapper.insert(carInfo);
    }

    @Override
    public void update(Long id, CarInfoSaveDTO saveDTO) {
        CarInfo carInfo = getCarInfoOrThrow(id);
        validateSaveDTO(saveDTO);
        ensureUnique(id, saveDTO.getCarCode(), saveDTO.getLicensePlate());
        BeanUtils.copyProperties(saveDTO, carInfo);
        normalizeNumericFields(carInfo);
        carInfoMapper.updateById(carInfo);
    }

    @Override
    public void delete(Long id) {
        getCarInfoOrThrow(id);
        carInfoMapper.deleteById(id);
    }

    @Override
    public void changeStatus(Long id, String status) {
        CarInfo carInfo = getCarInfoOrThrow(id);
        validateStatus(status);
        carInfo.setStatus(status);
        carInfoMapper.updateById(carInfo);
    }

    private List<CarInfoVO> convertToVO(List<CarInfo> records) {
        Map<Long, String> regionNameMap = buildRegionNameMap(records);
        return records.stream().map(item -> convertToVO(item, regionNameMap)).collect(Collectors.toList());
    }

    private CarInfoVO convertToVO(CarInfo carInfo) {
        return convertToVO(carInfo, buildRegionNameMap(List.of(carInfo)));
    }

    private CarInfoVO convertToVO(CarInfo carInfo, Map<Long, String> regionNameMap) {
        return CarInfoVO.builder()
                .id(carInfo.getId())
                .carCode(carInfo.getCarCode())
                .licensePlate(carInfo.getLicensePlate())
                .brand(carInfo.getBrand())
                .model(carInfo.getModel())
                .color(carInfo.getColor())
                .seatCount(carInfo.getSeatCount())
                .energyType(carInfo.getEnergyType())
                .dailyRent(carInfo.getDailyRent())
                .deposit(carInfo.getDeposit())
                .currentRegionId(carInfo.getCurrentRegionId())
                .currentRegionName(regionNameMap.get(carInfo.getCurrentRegionId()))
                .status(carInfo.getStatus())
                .mileage(carInfo.getMileage())
                .batteryLevel(carInfo.getBatteryLevel())
                .remark(carInfo.getRemark())
                .createTime(carInfo.getCreateTime())
                .updateTime(carInfo.getUpdateTime())
                .build();
    }

    private Map<Long, String> buildRegionNameMap(List<CarInfo> records) {
        List<Long> regionIds = records.stream()
                .map(CarInfo::getCurrentRegionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (regionIds.isEmpty()) {
            return Map.of();
        }
        return regionMapper.selectBatchIds(regionIds).stream()
                .collect(Collectors.toMap(Region::getId, Region::getRegionName, (left, right) -> left));
    }

    private CarInfo getCarInfoOrThrow(Long id) {
        CarInfo carInfo = carInfoMapper.selectById(id);
        if (carInfo == null) {
            throw new BusinessException(404, "车辆不存在");
        }
        return carInfo;
    }

    private void ensureUnique(Long id, String carCode, String licensePlate) {
        List<CarInfo> carInfos = carInfoMapper.selectList(new LambdaQueryWrapper<CarInfo>()
                .and(wrapper -> wrapper.eq(CarInfo::getCarCode, carCode)
                        .or()
                        .eq(CarInfo::getLicensePlate, licensePlate)));
        boolean duplicated = carInfos.stream().anyMatch(item -> !Objects.equals(item.getId(), id));
        if (duplicated) {
            throw new BusinessException(409, "车辆编号或车牌号已存在");
        }
    }

    private void validateSaveDTO(CarInfoSaveDTO saveDTO) {
        validateStatus(saveDTO.getStatus());
        if (saveDTO.getSeatCount() == null || saveDTO.getSeatCount() <= 0) {
            throw new BusinessException(400, "座位数必须大于 0");
        }
        if (saveDTO.getBatteryLevel() != null && (saveDTO.getBatteryLevel() < 0 || saveDTO.getBatteryLevel() > 100)) {
            throw new BusinessException(400, "电量仅支持 0 到 100");
        }
        if (saveDTO.getCurrentRegionId() != null && regionMapper.selectById(saveDTO.getCurrentRegionId()) == null) {
            throw new BusinessException(400, "所属区域不存在");
        }
    }

    private void validateStatus(String status) {
        if (!StringUtils.hasText(status) || !ALLOWED_STATUS.contains(status)) {
            throw new BusinessException(400, "车辆状态不合法");
        }
    }

    private void normalizeNumericFields(CarInfo carInfo) {
        if (carInfo.getMileage() == null) {
            carInfo.setMileage(java.math.BigDecimal.ZERO);
        }
    }
}