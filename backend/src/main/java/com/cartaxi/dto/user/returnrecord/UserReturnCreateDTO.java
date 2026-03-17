package com.cartaxi.dto.user.returnrecord;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserReturnCreateDTO {

    @NotNull(message = "??????")
    private Long rentalOrderId;

    @NotNull(message = "????????")
    private Long returnRegionId;

    @NotBlank(message = "????????")
    private String vehicleCondition;

    private String remark;
}
