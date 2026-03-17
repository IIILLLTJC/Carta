package com.cartaxi.vo.admin.recovery;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarRecoveryRecordVO {

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
    private LocalDateTime recoveryTime;
    private String recoveryReason;
    private String status;
    private String remark;
    private LocalDateTime createTime;
}