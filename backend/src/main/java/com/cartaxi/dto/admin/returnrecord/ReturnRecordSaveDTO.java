package com.cartaxi.dto.admin.returnrecord;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ReturnRecordSaveDTO {

    @NotNull(message = "订单不能为空")
    private Long rentalOrderId;

    @NotNull(message = "归还区域不能为空")
    private Long returnRegionId;

    @NotBlank(message = "归还状态不能为空")
    private String status;

    @Size(max = 255, message = "车辆状况长度不能超过255")
    private String vehicleCondition;

    @NotNull(message = "损坏费用不能为空")
    @DecimalMin(value = "0.00", message = "损坏费用不能小于0")
    private BigDecimal damageCost;

    @NotNull(message = "逾期费用不能为空")
    @DecimalMin(value = "0.00", message = "逾期费用不能小于0")
    private BigDecimal lateFee;

    @NotNull(message = "最终结算金额不能为空")
    @DecimalMin(value = "0.00", message = "最终结算金额不能小于0")
    private BigDecimal finalAmount;

    private Long processedBy;

    @NotBlank(message = "归还时间不能为空")
    private String returnTime;

    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;
}