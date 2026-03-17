package com.cartaxi.dto.admin.recovery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CarRecoveryRecordSaveDTO {

    @NotNull(message = "车辆不能为空")
    private Long carId;

    private Long regionId;

    @NotNull(message = "操作管理员不能为空")
    private Long operatorAdminId;

    @NotBlank(message = "回收时间不能为空")
    private String recoveryTime;

    @Size(max = 255, message = "回收原因长度不能超过255")
    private String recoveryReason;

    @NotBlank(message = "回收状态不能为空")
    private String status;

    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;
}