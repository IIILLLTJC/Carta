package com.cartaxi.service;

import com.cartaxi.dto.admin.car.CarInfoQueryDTO;
import com.cartaxi.dto.admin.car.CarInfoSaveDTO;
import com.cartaxi.vo.admin.car.CarInfoVO;
import com.cartaxi.vo.common.PageResult;

public interface AdminCarInfoService {

    PageResult<CarInfoVO> page(CarInfoQueryDTO queryDTO);

    CarInfoVO detail(Long id);

    void create(CarInfoSaveDTO saveDTO);

    void update(Long id, CarInfoSaveDTO saveDTO);

    void delete(Long id);

    void changeStatus(Long id, String status);
}