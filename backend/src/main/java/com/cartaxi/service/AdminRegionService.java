package com.cartaxi.service;

import com.cartaxi.dto.admin.region.RegionQueryDTO;
import com.cartaxi.dto.admin.region.RegionSaveDTO;
import com.cartaxi.entity.Region;
import com.cartaxi.vo.admin.region.RegionOptionVO;
import com.cartaxi.vo.common.PageResult;
import java.util.List;

public interface AdminRegionService {

    PageResult<Region> page(RegionQueryDTO queryDTO);

    Region detail(Long id);

    void create(RegionSaveDTO saveDTO);

    void update(Long id, RegionSaveDTO saveDTO);

    void delete(Long id);

    void changeStatus(Long id, Integer status);

    List<RegionOptionVO> options();
}