package com.cartaxi.service;

import com.cartaxi.dto.admin.admin.SysAdminQueryDTO;
import com.cartaxi.dto.admin.admin.SysAdminSaveDTO;
import com.cartaxi.vo.admin.admin.SysAdminVO;
import com.cartaxi.vo.common.PageResult;

public interface AdminSysAdminService {

    PageResult<SysAdminVO> page(SysAdminQueryDTO queryDTO);

    SysAdminVO detail(Long id);

    void create(SysAdminSaveDTO saveDTO);

    void update(Long id, SysAdminSaveDTO saveDTO);

    void delete(Long id);

    void changeStatus(Long id, Integer status);
}