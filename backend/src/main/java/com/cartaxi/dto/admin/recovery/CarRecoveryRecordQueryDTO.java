package com.cartaxi.dto.admin.recovery;

import lombok.Data;

@Data
public class CarRecoveryRecordQueryDTO {

    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String carKeyword;
    private Long regionId;
    private String status;
    private Long operatorAdminId;
}