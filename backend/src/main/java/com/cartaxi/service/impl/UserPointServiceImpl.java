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
        LambdaQueryWrapper<Region> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Region::getStatus, 1)
                .like(StringUtils.hasText(queryDTO.getRegionName()), Region::getRegionName, queryDTO.getRegionName())
                .orderByAsc(Region::getRegionCode);
        Page<Region> result = regionMapper.selectPage(page, wrapper);
        List<Long> regionIds = result.getRecords().stream().map(Region::getId).filter(Objects::nonNull).toList();
        if (regionIds.isEmpty()) {
            return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), Collections.emptyList());
        }
        Map<Long, List<CarInfo>> carMap = carInfoMapper.selectList(new LambdaQueryWrapper<CarInfo>()
                        .in(CarInfo::getCurrentRegionId, regionIds)
                        .eq(CarInfo::getStatus, "DEPLOYED")
                        .orderByAsc(CarInfo::getCarCode))
                .stream()
                .collect(Collectors.groupingBy(CarInfo::getCurrentRegionId));
        List<UserPointVO> records = result.getRecords().stream().map(region -> {
            List<UserPointCarVO> cars = carMap.getOrDefault(region.getId(), Collections.emptyList()).stream()
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
                    .availableCarCount(cars.size())
                    .availableCars(cars)
                    .build();
        }).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }
}