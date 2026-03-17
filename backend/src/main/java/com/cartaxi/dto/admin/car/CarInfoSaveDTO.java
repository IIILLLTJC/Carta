package com.cartaxi.dto.admin.car;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CarInfoSaveDTO {

    @NotBlank(message = "车辆编号不能为空")
    @Size(max = 50, message = "车辆编号长度不能超过50")
    private String carCode;

    @NotBlank(message = "车牌号不能为空")
    @Size(max = 30, message = "车牌号长度不能超过30")
    private String licensePlate;

    @NotBlank(message = "品牌不能为空")
    @Size(max = 50, message = "品牌长度不能超过50")
    private String brand;

    @NotBlank(message = "型号不能为空")
    @Size(max = 100, message = "型号长度不能超过100")
    private String model;

    @Size(max = 30, message = "颜色长度不能超过30")
    private String color;

    @NotNull(message = "座位数不能为空")
    private Integer seatCount;

    @Size(max = 30, message = "能源类型长度不能超过30")
    private String energyType;

    @NotNull(message = "日租金不能为空")
    @DecimalMin(value = "0.00", message = "日租金不能小于0")
    private BigDecimal dailyRent;

    @NotNull(message = "押金不能为空")
    @DecimalMin(value = "0.00", message = "押金不能小于0")
    private BigDecimal deposit;

    private Long currentRegionId;

    @NotBlank(message = "车辆状态不能为空")
    private String status;

    @DecimalMin(value = "0.00", message = "里程不能小于0")
    private BigDecimal mileage;

    private Integer batteryLevel;

    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;
}