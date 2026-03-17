package com.cartaxi.vo.admin.order;

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
public class RentalOrderVO {

    private Long id;
    private String orderNo;
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private Long carId;
    private String carCode;
    private String licensePlate;
    private String brand;
    private String model;
    private Long pickupRegionId;
    private String pickupRegionName;
    private Long returnRegionId;
    private String returnRegionName;
    private String orderStatus;
    private BigDecimal orderAmount;
    private BigDecimal depositAmount;
    private LocalDateTime startTime;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime actualReturnTime;
    private String paymentStatus;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}