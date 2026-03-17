package com.cartaxi.dto.common;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IntegerStatusUpdateDTO {

    @NotNull(message = "状态不能为空")
    private Integer status;
}