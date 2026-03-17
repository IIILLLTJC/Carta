package com.cartaxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("car_info")
public class CarInfo {

    @TableId(type = IdType.AUTO)
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
    private String status;
    private BigDecimal mileage;
    private Integer batteryLevel;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
