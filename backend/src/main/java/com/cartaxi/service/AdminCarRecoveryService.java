package com.cartaxi.service;

import com.cartaxi.dto.admin.recovery.CarRecoveryRecordQueryDTO;
import com.cartaxi.dto.admin.recovery.CarRecoveryRecordSaveDTO;
import com.cartaxi.vo.admin.recovery.CarRecoveryFormOptionsVO;
import com.cartaxi.vo.admin.recovery.CarRecoveryRecordVO;
import com.cartaxi.vo.common.PageResult;

public interface AdminCarRecoveryService {

    PageResult<CarRecoveryRecordVO> page(CarRecoveryRecordQueryDTO queryDTO);

    CarRecoveryRecordVO detail(Long id);

    void create(CarRecoveryRecordSaveDTO saveDTO);

    void update(Long id, CarRecoveryRecordSaveDTO saveDTO);

    void delete(Long id);

    void changeStatus(Long id, String status);

    CarRecoveryFormOptionsVO formOptions();
}