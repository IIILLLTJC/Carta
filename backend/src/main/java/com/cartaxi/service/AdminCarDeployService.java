package com.cartaxi.service;

import com.cartaxi.dto.admin.deploy.CarDeployRecordQueryDTO;
import com.cartaxi.dto.admin.deploy.CarDeployRecordSaveDTO;
import com.cartaxi.vo.admin.deploy.CarDeployFormOptionsVO;
import com.cartaxi.vo.admin.deploy.CarDeployRecordVO;
import com.cartaxi.vo.common.PageResult;

public interface AdminCarDeployService {

    PageResult<CarDeployRecordVO> page(CarDeployRecordQueryDTO queryDTO);

    CarDeployRecordVO detail(Long id);

    void create(CarDeployRecordSaveDTO saveDTO);

    void update(Long id, CarDeployRecordSaveDTO saveDTO);

    void delete(Long id);

    void changeStatus(Long id, String status);

    CarDeployFormOptionsVO formOptions();
}