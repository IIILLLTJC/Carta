package com.cartaxi.service;

import com.cartaxi.dto.admin.order.RentalOrderQueryDTO;
import com.cartaxi.dto.admin.order.RentalOrderSaveDTO;
import com.cartaxi.vo.admin.order.RentalOrderFormOptionsVO;
import com.cartaxi.vo.admin.order.RentalOrderVO;
import com.cartaxi.vo.common.PageResult;

public interface AdminRentalOrderService {

    PageResult<RentalOrderVO> page(RentalOrderQueryDTO queryDTO);

    RentalOrderVO detail(Long id);

    void create(RentalOrderSaveDTO saveDTO);

    void update(Long id, RentalOrderSaveDTO saveDTO);

    void delete(Long id);

    void changeStatus(Long id, String status);

    RentalOrderFormOptionsVO formOptions();
}