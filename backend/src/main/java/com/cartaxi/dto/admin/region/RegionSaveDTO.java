package com.cartaxi.dto.admin.region;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegionSaveDTO {

    @NotBlank(message = "\u533a\u57df\u540d\u79f0\u4e0d\u80fd\u4e3a\u7a7a")
    @Size(max = 100, message = "\u533a\u57df\u540d\u79f0\u957f\u5ea6\u4e0d\u80fd\u8d85\u8fc7100")
    private String regionName;

    @NotBlank(message = "\u533a\u57df\u7f16\u7801\u4e0d\u80fd\u4e3a\u7a7a")
    @Size(max = 50, message = "\u533a\u57df\u7f16\u7801\u957f\u5ea6\u4e0d\u80fd\u8d85\u8fc750")
    private String regionCode;

    @NotBlank(message = "\u8be6\u7ec6\u5730\u5740\u4e0d\u80fd\u4e3a\u7a7a")
    @Size(max = 255, message = "\u8be6\u7ec6\u5730\u5740\u957f\u5ea6\u4e0d\u80fd\u8d85\u8fc7255")
    private String address;

    @DecimalMin(value = "-180", message = "\u7ecf\u5ea6\u5fc5\u987b\u5728 -180 \u5230 180 \u4e4b\u95f4")
    @DecimalMax(value = "180", message = "\u7ecf\u5ea6\u5fc5\u987b\u5728 -180 \u5230 180 \u4e4b\u95f4")
    private Double longitude;

    @DecimalMin(value = "-90", message = "\u7eac\u5ea6\u5fc5\u987b\u5728 -90 \u5230 90 \u4e4b\u95f4")
    @DecimalMax(value = "90", message = "\u7eac\u5ea6\u5fc5\u987b\u5728 -90 \u5230 90 \u4e4b\u95f4")
    private Double latitude;

    @NotNull(message = "\u72b6\u6001\u4e0d\u80fd\u4e3a\u7a7a")
    private Integer status;

    @Size(max = 255, message = "\u5907\u6ce8\u957f\u5ea6\u4e0d\u80fd\u8d85\u8fc7255")
    private String remark;
}
