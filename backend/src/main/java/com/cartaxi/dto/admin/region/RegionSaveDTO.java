package com.cartaxi.dto.admin.region;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegionSaveDTO {

    @NotBlank(message = "区域名称不能为空")
    @Size(max = 100, message = "区域名称长度不能超过100")
    private String regionName;

    @NotBlank(message = "区域编码不能为空")
    @Size(max = 50, message = "区域编码长度不能超过50")
    private String regionCode;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "详细地址长度不能超过255")
    private String address;

    private Double longitude;
    private Double latitude;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;
}