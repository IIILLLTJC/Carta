package com.cartaxi.vo.user.returnrecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserReturnVO {

    private Long id;
    private Long rentalOrderId;
    private String orderNo;
    private Long carId;
    private String carCode;
    private String licensePlate;
    private String brand;
    private String model;
    private Long returnRegionId;
    private String returnRegionName;
    private String status;
    private String vehicleCondition;
    private BigDecimal orderAmount;
    private BigDecimal depositAmount;
    private BigDecimal damageCost;
    private BigDecimal lateFee;
    private BigDecimal finalAmount;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime actualReturnTime;
    private LocalDateTime returnTime;
    private LocalDateTime createTime;
    private String remark;
}
