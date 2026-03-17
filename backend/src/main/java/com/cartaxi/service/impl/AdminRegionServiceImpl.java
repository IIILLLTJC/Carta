package com.cartaxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cartaxi.common.exception.BusinessException;
import com.cartaxi.dto.admin.region.RegionQueryDTO;
import com.cartaxi.dto.admin.region.RegionSaveDTO;
import com.cartaxi.entity.CarInfo;
import com.cartaxi.entity.Region;
import com.cartaxi.mapper.CarInfoMapper;
import com.cartaxi.mapper.RegionMapper;
import com.cartaxi.service.AdminRegionService;
import com.cartaxi.vo.admin.region.RegionOptionVO;
import com.cartaxi.vo.common.PageResult;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminRegionServiceImpl implements AdminRegionService {

    private final RegionMapper regionMapper;
    private final CarInfoMapper carInfoMapper;

    public AdminRegionServiceImpl(RegionMapper regionMapper, CarInfoMapper carInfoMapper) {
        this.regionMapper = regionMapper;
        this.carInfoMapper = carInfoMapper;
    }

    @Override
    public PageResult<Region> page(RegionQueryDTO queryDTO) {
        Page<Region> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Region> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getRegionName()), Region::getRegionName, queryDTO.getRegionName())
                .like(StringUtils.hasText(queryDTO.getRegionCode()), Region::getRegionCode, queryDTO.getRegionCode())
                .eq(queryDTO.getStatus() != null, Region::getStatus, queryDTO.getStatus())
                .orderByDesc(Region::getCreateTime);
        return PageResult.of(regionMapper.selectPage(page, wrapper));
    }

    @Override
    public Region detail(Long id) {
        return getRegionOrThrow(id);
    }

    @Override
    public void create(RegionSaveDTO saveDTO) {
        validateStatus(saveDTO.getStatus());
        ensureUnique(null, saveDTO.getRegionName(), saveDTO.getRegionCode());
        Region region = new Region();
        BeanUtils.copyProperties(saveDTO, region);
        regionMapper.insert(region);
    }

    @Override
    public void update(Long id, RegionSaveDTO saveDTO) {
        Region region = getRegionOrThrow(id);
        validateStatus(saveDTO.getStatus());
        ensureUnique(id, saveDTO.getRegionName(), saveDTO.getRegionCode());
        BeanUtils.copyProperties(saveDTO, region);
        regionMapper.updateById(region);
    }

    @Override
    public void delete(Long id) {
        getRegionOrThrow(id);
        Long carCount = carInfoMapper.selectCount(new LambdaQueryWrapper<CarInfo>()
                .eq(CarInfo::getCurrentRegionId, id));
        if (carCount != null && carCount > 0) {
            throw new BusinessException(400, "当前区域下存在车辆，无法删除");
        }
        regionMapper.deleteById(id);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        Region region = getRegionOrThrow(id);
        validateStatus(status);
        region.setStatus(status);
        regionMapper.updateById(region);
    }

    @Override
    public List<RegionOptionVO> options() {
        List<Region> regions = regionMapper.selectList(new LambdaQueryWrapper<Region>()
                .eq(Region::getStatus, 1)
                .orderByAsc(Region::getRegionCode));
        return regions.stream()
                .map(item -> new RegionOptionVO(item.getId(), item.getRegionName(), item.getRegionCode()))
                .collect(Collectors.toList());
    }

    private Region getRegionOrThrow(Long id) {
        Region region = regionMapper.selectById(id);
        if (region == null) {
            throw new BusinessException(404, "区域不存在");
        }
        return region;
    }

    private void ensureUnique(Long id, String regionName, String regionCode) {
        List<Region> regions = regionMapper.selectList(new LambdaQueryWrapper<Region>()
                .and(wrapper -> wrapper.eq(Region::getRegionName, regionName)
                        .or()
                        .eq(Region::getRegionCode, regionCode)));
        boolean duplicated = regions.stream().anyMatch(item -> !Objects.equals(item.getId(), id));
        if (duplicated) {
            throw new BusinessException(409, "区域名称或区域编码已存在");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "区域状态仅支持 0 或 1");
        }
    }
}