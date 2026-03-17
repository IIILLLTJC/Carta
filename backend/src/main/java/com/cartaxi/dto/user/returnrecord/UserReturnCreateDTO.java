package com.cartaxi.dto.user.returnrecord;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserReturnCreateDTO {

    @NotNull(message = "请选择租赁订单")
    private Long rentalOrderId;

    @NotNull(message = "请选择归还区域")
    private Long returnRegionId;

    @NotBlank(message = "请选择车辆自报情况")
    private String vehicleCondition;

    private String remark;
}