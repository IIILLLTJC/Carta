package com.cartaxi.dto.user.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserOrderCreateDTO {

    @NotNull(message = "车辆不能为空")
    private Long carId;

    private Long returnRegionId;

    @NotBlank(message = "预计归还时间不能为空")
    private String expectedReturnTime;

    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;
}