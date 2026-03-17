package com.cartaxi.dto.admin.order;

import lombok.Data;

@Data
public class RentalOrderQueryDTO {

    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String orderNo;
    private String userKeyword;
    private String carKeyword;
    private String orderStatus;
    private String paymentStatus;
}