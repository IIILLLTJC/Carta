package com.cartaxi.service;

import com.cartaxi.dto.user.order.UserOrderCreateDTO;
import com.cartaxi.dto.user.order.UserOrderQueryDTO;
import com.cartaxi.vo.common.PageResult;
import com.cartaxi.vo.user.order.UserOrderFormOptionsVO;
import com.cartaxi.vo.user.order.UserOrderVO;

public interface UserOrderService {

    PageResult<UserOrderVO> page(UserOrderQueryDTO queryDTO);

    UserOrderVO detail(Long id);

    void create(UserOrderCreateDTO createDTO);

    void mockPay(Long id);

    UserOrderFormOptionsVO formOptions();
}
