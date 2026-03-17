package com.cartaxi.vo.admin.deploy;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarDeployRecordVO {

    private Long id;
    private Long carId;
    private String carCode;
    private String licensePlate;
    private String brand;
    private String model;
    private Long regionId;
    private String regionName;
    private Long operatorAdminId;
    private String operatorAdminName;
    private LocalDateTime deployTime;
    private String status;
    private String remark;
    private LocalDateTime createTime;
}