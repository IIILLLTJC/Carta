package com.cartaxi.service;

import com.cartaxi.dto.user.returnrecord.UserReturnCreateDTO;
import com.cartaxi.dto.user.returnrecord.UserReturnQueryDTO;
import com.cartaxi.vo.common.PageResult;
import com.cartaxi.vo.user.returnrecord.UserReturnFormOptionsVO;
import com.cartaxi.vo.user.returnrecord.UserReturnVO;

public interface UserReturnService {

    PageResult<UserReturnVO> page(UserReturnQueryDTO queryDTO);

    UserReturnVO detail(Long id);

    UserReturnFormOptionsVO formOptions();

    void create(UserReturnCreateDTO createDTO);
}
