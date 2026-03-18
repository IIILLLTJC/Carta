package com.cartaxi.service;

import com.cartaxi.dto.user.point.UserPointQueryDTO;
import com.cartaxi.vo.common.PageResult;
import com.cartaxi.vo.user.point.UserPointMapVO;
import com.cartaxi.vo.user.point.UserPointVO;
import java.util.List;

public interface UserPointService {

    PageResult<UserPointVO> page(UserPointQueryDTO queryDTO);

    List<UserPointMapVO> mapPoints(String regionName);
}
