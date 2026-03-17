package com.cartaxi.dto.user.returnrecord;

import lombok.Data;

@Data
public class UserReturnQueryDTO {

    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String orderNo;
    private String status;
}
