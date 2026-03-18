package com.cartaxi.vo.admin.car;

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
public class CarInfoVO {

    private Long id;
    private String carCode;
    private String licensePlate;
    private String brand;
    private String model;
    private String color;
    private Integer seatCount;
    private String energyType;
    private BigDecimal dailyRent;
    private BigDecimal deposit;
    private Long currentRegionId;
    private String currentRegionName;
    private String status;
    private BigDecimal mileage;
    private Integer batteryLevel;
    private String imageUrl;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
