package com.cartaxi.dto.admin.car;

import lombok.Data;

@Data
public class CarInfoQueryDTO {

    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String carCode;
    private String licensePlate;
    private String brand;
    private String model;
    private String status;
    private Long currentRegionId;
}