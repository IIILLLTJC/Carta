package com.cartaxi.dto.admin.deploy;

import lombok.Data;

@Data
public class CarDeployRecordQueryDTO {

    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String carKeyword;
    private Long regionId;
    private String status;
    private Long operatorAdminId;
}