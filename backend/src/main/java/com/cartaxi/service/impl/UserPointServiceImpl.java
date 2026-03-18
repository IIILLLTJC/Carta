package com.cartaxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cartaxi.dto.user.point.UserPointQueryDTO;
import com.cartaxi.entity.CarInfo;
import com.cartaxi.entity.Region;
import com.cartaxi.mapper.CarInfoMapper;
import com.cartaxi.mapper.RegionMapper;
import com.cartaxi.service.UserPointService;
import com.cartaxi.vo.common.PageResult;
import com.cartaxi.vo.user.point.UserPointCarVO;
import com.cartaxi.vo.user.point.UserPointMapVO;
import com.cartaxi.vo.user.point.UserPointVO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserPointServiceImpl implements UserPointService {

    private final RegionMapper regionMapper;
    private final CarInfoMapper carInfoMapper;

    public UserPointServiceImpl(RegionMapper regionMapper, CarInfoMapper carInfoMapper) {
        this.regionMapper = regionMapper;
        this.carInfoMapper = carInfoMapper;
    }

    @Override
    public PageResult<UserPointVO> page(UserPointQueryDTO queryDTO) {
        Page<Region> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Region> wrapper = buildRegionWrapper(queryDTO.getRegionName());
        Page<Region> result = regionMapper.selectPage(page, wrapper);
        List<Region> regions = result.getRecords();
        if (regions.isEmpty()) {
            return PageResult.empty(queryDTO.getPageNum(), queryDTO.getPageSize());
        }
        Map<Long, List<CarInfo>> carMap = mapAvailableCars(regions.stream()
                .map(Region::getId)
                .filter(Objects::nonNull)
                .toList());
        List<UserPointVO> records = regions.stream()
                .map(region -> toPointVO(region, carMap.getOrDefault(region.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public List<UserPointMapVO> mapPoints(String regionName) {
        List<Region> regions = regionMapper.selectList(buildRegionWrapper(regionName));
        if (regions.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, List<CarInfo>> carMap = mapAvailableCars(regions.stream()
                .map(Region::getId)
                .filter(Objects::nonNull)
                .toList());
        return regions.stream()
                .map(region -> UserPointMapVO.builder()
                        .id(region.getId())
                        .regionName(region.getRegionName())
                        .regionCode(region.getRegionCode())
                        .address(region.getAddress())
                        .longitude(region.getLongitude())
                        .latitude(region.getLatitude())
                        .availableCarCount(carMap.getOrDefault(region.getId(), Collections.emptyList()).size())
                        .build())
                .collect(Collectors.toList());
    }

    private LambdaQueryWrapper<Region> buildRegionWrapper(String regionName) {
        LambdaQueryWrapper<Region> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Region::getStatus, 1)
                .like(StringUtils.hasText(regionName), Region::getRegionName, regionName)
                .orderByAsc(Region::getRegionCode);
        return wrapper;
    }

    private Map<Long, List<CarInfo>> mapAvailableCars(List<Long> regionIds) {
        if (regionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return carInfoMapper.selectList(new LambdaQueryWrapper<CarInfo>()
                        .in(CarInfo::getCurrentRegionId, regionIds)
                        .eq(CarInfo::getStatus, "DEPLOYED")
                        .orderByAsc(CarInfo::getCarCode))
                .stream()
                .collect(Collectors.groupingBy(CarInfo::getCurrentRegionId));
    }

    private UserPointVO toPointVO(Region region, List<CarInfo> cars) {
        List<UserPointCarVO> availableCars = cars.stream()
                .map(car -> UserPointCarVO.builder()
                        .id(car.getId())
                        .carCode(car.getCarCode())
                        .licensePlate(car.getLicensePlate())
                        .brand(car.getBrand())
                        .model(car.getModel())
                        .color(car.getColor())
                        .seatCount(car.getSeatCount())
                        .energyType(car.getEnergyType())
                        .dailyRent(car.getDailyRent())
                        .deposit(car.getDeposit())
                        .batteryLevel(car.getBatteryLevel())
                        .status(car.getStatus())
                        .build())
                .collect(Collectors.toList());
        return UserPointVO.builder()
                .id(region.getId())
                .regionName(region.getRegionName())
                .regionCode(region.getRegionCode())
                .address(region.getAddress())
                .longitude(region.getLongitude())
                .latitude(region.getLatitude())
                .availableCarCount(availableCars.size())
                .availableCars(availableCars)
                .build();
    }
}
