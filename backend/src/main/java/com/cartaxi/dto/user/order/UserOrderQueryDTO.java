package com.cartaxi.dto.user.order;

import lombok.Data;

@Data
public class UserOrderQueryDTO {

    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String orderNo;
    private String orderStatus;
}