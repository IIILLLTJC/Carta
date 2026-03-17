package com.cartaxi.vo.user.order;

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
public class UserOrderVO {

    private Long id;
    private String orderNo;
    private String orderStatus;
    private String paymentStatus;
    private Long carId;
    private String carCode;
    private String licensePlate;
    private String brand;
    private String model;
    private Long pickupRegionId;
    private String pickupRegionName;
    private Long returnRegionId;
    private String returnRegionName;
    private BigDecimal orderAmount;
    private BigDecimal depositAmount;
    private LocalDateTime startTime;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime actualReturnTime;
    private String remark;
    private LocalDateTime createTime;
}