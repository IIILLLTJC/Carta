package com.cartaxi.service;

import com.cartaxi.dto.admin.user.SysUserQueryDTO;
import com.cartaxi.dto.admin.user.SysUserSaveDTO;
import com.cartaxi.vo.admin.user.SysUserVO;
import com.cartaxi.vo.common.PageResult;

public interface AdminUserService {

    PageResult<SysUserVO> page(SysUserQueryDTO queryDTO);

    SysUserVO detail(Long id);

    void create(SysUserSaveDTO saveDTO);

    void update(Long id, SysUserSaveDTO saveDTO);

    void delete(Long id);

    void changeStatus(Long id, Integer status);
}