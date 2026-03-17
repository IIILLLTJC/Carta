package com.cartaxi.dto.admin.returnrecord;

import lombok.Data;

@Data
public class ReturnRecordQueryDTO {

    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String orderNo;
    private String carKeyword;
    private String userKeyword;
    private Long returnRegionId;
    private String status;
}