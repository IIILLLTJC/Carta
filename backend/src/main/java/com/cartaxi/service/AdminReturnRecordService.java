package com.cartaxi.service;

import com.cartaxi.dto.admin.returnrecord.ReturnRecordQueryDTO;
import com.cartaxi.dto.admin.returnrecord.ReturnRecordSaveDTO;
import com.cartaxi.vo.admin.returnrecord.ReturnRecordFormOptionsVO;
import com.cartaxi.vo.admin.returnrecord.ReturnRecordVO;
import com.cartaxi.vo.common.PageResult;

public interface AdminReturnRecordService {

    PageResult<ReturnRecordVO> page(ReturnRecordQueryDTO queryDTO);

    ReturnRecordVO detail(Long id);

    void create(ReturnRecordSaveDTO saveDTO);

    void update(Long id, ReturnRecordSaveDTO saveDTO);

    void delete(Long id);

    void changeStatus(Long id, String status);

    ReturnRecordFormOptionsVO formOptions();
}