package com.cartaxi.dto.admin.deploy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CarDeployRecordSaveDTO {

    @NotNull(message = "车辆不能为空")
    private Long carId;

    @NotNull(message = "投放区域不能为空")
    private Long regionId;

    @NotNull(message = "操作管理员不能为空")
    private Long operatorAdminId;

    @NotBlank(message = "投放时间不能为空")
    private String deployTime;

    @NotBlank(message = "投放状态不能为空")
    private String status;

    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;
}