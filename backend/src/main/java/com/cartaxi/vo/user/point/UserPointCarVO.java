package com.cartaxi.vo.user.point;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointCarVO {

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
    private Integer batteryLevel;
    private String status;
}