package com.cartaxi.service;

import com.cartaxi.dto.user.point.UserPointQueryDTO;
import com.cartaxi.vo.common.PageResult;
import com.cartaxi.vo.user.point.UserPointVO;

public interface UserPointService {

    PageResult<UserPointVO> page(UserPointQueryDTO queryDTO);
}