package com.cartaxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cartaxi.common.exception.BusinessException;
import com.cartaxi.common.util.DateTimeUtil;
import com.cartaxi.dto.admin.deploy.CarDeployRecordQueryDTO;
import com.cartaxi.dto.admin.deploy.CarDeployRecordSaveDTO;
import com.cartaxi.entity.CarDeployRecord;
import com.cartaxi.entity.CarInfo;
import com.cartaxi.entity.Region;
import com.cartaxi.entity.SysAdmin;
import com.cartaxi.mapper.CarDeployRecordMapper;
import com.cartaxi.mapper.CarInfoMapper;
import com.cartaxi.mapper.RegionMapper;
import com.cartaxi.mapper.SysAdminMapper;
import com.cartaxi.service.AdminCarDeployService;
import com.cartaxi.vo.admin.deploy.AdminSimpleOptionVO;
import com.cartaxi.vo.admin.deploy.CarDeployFormOptionsVO;
import com.cartaxi.vo.admin.deploy.CarDeployRecordVO;
import com.cartaxi.vo.admin.deploy.CarSimpleOptionVO;
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
public class AdminCarDeployServiceImpl implements AdminCarDeployService {

    private static final Set<String> ALLOWED_STATUS = Set.of("DEPLOYED", "PENDING", "CANCELLED");

    private final CarDeployRecordMapper deployRecordMapper;
    private final CarInfoMapper carInfoMapper;
    private final RegionMapper regionMapper;
    private final SysAdminMapper sysAdminMapper;

    public AdminCarDeployServiceImpl(CarDeployRecordMapper deployRecordMapper,
                                     CarInfoMapper carInfoMapper,
                                     RegionMapper regionMapper,
                                     SysAdminMapper sysAdminMapper) {
        this.deployRecordMapper = deployRecordMapper;
        this.carInfoMapper = carInfoMapper;
        this.regionMapper = regionMapper;
        this.sysAdminMapper = sysAdminMapper;
    }

    @Override
    public PageResult<CarDeployRecordVO> page(CarDeployRecordQueryDTO queryDTO) {
        List<Long> matchedCarIds = findMatchedCarIds(queryDTO.getCarKeyword());
        if (StringUtils.hasText(queryDTO.getCarKeyword()) && matchedCarIds.isEmpty()) {
            return PageResult.empty(queryDTO.getPageNum(), queryDTO.getPageSize());
        }

        Page<CarDeployRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<CarDeployRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(matchedCarIds != null && !matchedCarIds.isEmpty(), CarDeployRecord::getCarId, matchedCarIds)
                .eq(queryDTO.getRegionId() != null, CarDeployRecord::getRegionId, queryDTO.getRegionId())
                .eq(StringUtils.hasText(queryDTO.getStatus()), CarDeployRecord::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getOperatorAdminId() != null, CarDeployRecord::getOperatorAdminId, queryDTO.getOperatorAdminId())
                .orderByDesc(CarDeployRecord::getDeployTime)
                .orderByDesc(CarDeployRecord::getCreateTime);
        Page<CarDeployRecord> result = deployRecordMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), enrich(result.getRecords()));
    }

    @Override
    public CarDeployRecordVO detail(Long id) {
        return enrich(getDeployOrThrow(id));
    }

    @Override
    public void create(CarDeployRecordSaveDTO saveDTO) {
        validateSaveDTO(saveDTO);
        CarDeployRecord record = new CarDeployRecord();
        BeanUtils.copyProperties(saveDTO, record, "deployTime");
        record.setDeployTime(DateTimeUtil.parseDateTime(saveDTO.getDeployTime(), "投放时间"));
        deployRecordMapper.insert(record);
        syncCarDeployState(record);
    }

    @Override
    public void update(Long id, CarDeployRecordSaveDTO saveDTO) {
        CarDeployRecord record = getDeployOrThrow(id);
        validateSaveDTO(saveDTO);
        BeanUtils.copyProperties(saveDTO, record, "deployTime");
        record.setDeployTime(DateTimeUtil.parseDateTime(saveDTO.getDeployTime(), "投放时间"));
        deployRecordMapper.updateById(record);
        syncCarDeployState(record);
    }

    @Override
    public void delete(Long id) {
        getDeployOrThrow(id);
        deployRecordMapper.deleteById(id);
    }

    @Override
    public void changeStatus(Long id, String status) {
        validateStatus(status);
        CarDeployRecord record = getDeployOrThrow(id);
        record.setStatus(status);
        deployRecordMapper.updateById(record);
        syncCarDeployState(record);
    }

    @Override
    public CarDeployFormOptionsVO formOptions() {
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
        return new CarDeployFormOptionsVO(cars, regions, admins);
    }

    private CarDeployRecord getDeployOrThrow(Long id) {
        CarDeployRecord record = deployRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(404, "投放记录不存在");
        }
        return record;
    }

    private void validateSaveDTO(CarDeployRecordSaveDTO saveDTO) {
        validateStatus(saveDTO.getStatus());
        if (carInfoMapper.selectById(saveDTO.getCarId()) == null) {
            throw new BusinessException(400, "车辆不存在");
        }
        if (regionMapper.selectById(saveDTO.getRegionId()) == null) {
            throw new BusinessException(400, "区域不存在");
        }
        if (sysAdminMapper.selectById(saveDTO.getOperatorAdminId()) == null) {
            throw new BusinessException(400, "操作管理员不存在");
        }
    }

    private void validateStatus(String status) {
        if (!StringUtils.hasText(status) || !ALLOWED_STATUS.contains(status)) {
            throw new BusinessException(400, "投放状态不合法");
        }
    }

    private void syncCarDeployState(CarDeployRecord record) {
        if (!"DEPLOYED".equals(record.getStatus())) {
            return;
        }
        CarInfo carInfo = carInfoMapper.selectById(record.getCarId());
        if (carInfo == null) {
            return;
        }
        carInfo.setCurrentRegionId(record.getRegionId());
        carInfo.setStatus("DEPLOYED");
        carInfoMapper.updateById(carInfo);
    }

    private List<CarDeployRecordVO> enrich(List<CarDeployRecord> records) {
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, CarInfo> carMap = carInfoMapper.selectBatchIds(records.stream().map(CarDeployRecord::getCarId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(CarInfo::getId, item -> item));
        Map<Long, Region> regionMap = regionMapper.selectBatchIds(records.stream().map(CarDeployRecord::getRegionId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Region::getId, item -> item));
        Map<Long, SysAdmin> adminMap = sysAdminMapper.selectBatchIds(records.stream().map(CarDeployRecord::getOperatorAdminId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(SysAdmin::getId, item -> item));
        return records.stream().map(item -> toVO(item, carMap, regionMap, adminMap)).collect(Collectors.toList());
    }

    private CarDeployRecordVO enrich(CarDeployRecord record) {
        Map<Long, CarInfo> carMap = new HashMap<>();
        CarInfo carInfo = carInfoMapper.selectById(record.getCarId());
        if (carInfo != null) {
            carMap.put(carInfo.getId(), carInfo);
        }
        Map<Long, Region> regionMap = new HashMap<>();
        Region region = regionMapper.selectById(record.getRegionId());
        if (region != null) {
            regionMap.put(region.getId(), region);
        }
        Map<Long, SysAdmin> adminMap = new HashMap<>();
        SysAdmin admin = sysAdminMapper.selectById(record.getOperatorAdminId());
        if (admin != null) {
            adminMap.put(admin.getId(), admin);
        }
        return toVO(record, carMap, regionMap, adminMap);
    }

    private CarDeployRecordVO toVO(CarDeployRecord item,
                                   Map<Long, CarInfo> carMap,
                                   Map<Long, Region> regionMap,
                                   Map<Long, SysAdmin> adminMap) {
        CarInfo carInfo = carMap.get(item.getCarId());
        Region region = regionMap.get(item.getRegionId());
        SysAdmin admin = adminMap.get(item.getOperatorAdminId());
        return CarDeployRecordVO.builder()
                .id(item.getId())
                .carId(item.getCarId())
                .carCode(carInfo == null ? null : carInfo.getCarCode())
                .licensePlate(carInfo == null ? null : carInfo.getLicensePlate())
                .brand(carInfo == null ? null : carInfo.getBrand())
                .model(carInfo == null ? null : carInfo.getModel())
                .regionId(item.getRegionId())
                .regionName(region == null ? null : region.getRegionName())
                .operatorAdminId(item.getOperatorAdminId())
                .operatorAdminName(admin == null ? null : (StringUtils.hasText(admin.getRealName()) ? admin.getRealName() : admin.getUsername()))
                .deployTime(item.getDeployTime())
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