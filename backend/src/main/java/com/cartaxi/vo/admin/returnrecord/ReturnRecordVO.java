package com.cartaxi.vo.admin.returnrecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRecordVO {

    private Long id;
    private Long rentalOrderId;
    private String orderNo;
    private Long carId;
    private String carCode;
    private String licensePlate;
    private String brand;
    private String model;
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private Long returnRegionId;
    private String returnRegionName;
    private String status;
    private String vehicleCondition;
    private BigDecimal damageCost;
    private BigDecimal lateFee;
    private BigDecimal finalAmount;
    private Long processedBy;
    private String processedByName;
    private LocalDateTime returnTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String remark;
}