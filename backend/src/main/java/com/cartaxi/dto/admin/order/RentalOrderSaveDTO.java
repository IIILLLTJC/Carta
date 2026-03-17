package com.cartaxi.dto.admin.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class RentalOrderSaveDTO {

    @NotNull(message = "下单用户不能为空")
    private Long userId;

    @NotNull(message = "租赁车辆不能为空")
    private Long carId;

    @NotNull(message = "取车区域不能为空")
    private Long pickupRegionId;

    private Long returnRegionId;

    @NotBlank(message = "订单状态不能为空")
    private String orderStatus;

    @NotNull(message = "订单金额不能为空")
    @DecimalMin(value = "0.00", message = "订单金额不能小于0")
    private BigDecimal orderAmount;

    @NotNull(message = "押金金额不能为空")
    @DecimalMin(value = "0.00", message = "押金金额不能小于0")
    private BigDecimal depositAmount;

    @NotBlank(message = "租赁开始时间不能为空")
    private String startTime;

    @NotBlank(message = "预计归还时间不能为空")
    private String expectedReturnTime;

    private String actualReturnTime;

    @NotBlank(message = "支付状态不能为空")
    private String paymentStatus;

    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;
}