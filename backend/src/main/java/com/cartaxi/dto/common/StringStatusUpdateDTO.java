package com.cartaxi.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StringStatusUpdateDTO {

    @NotBlank(message = "状态不能为空")
    private String status;
}