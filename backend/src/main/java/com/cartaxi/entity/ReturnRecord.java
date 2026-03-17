package com.cartaxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("return_record")
public class ReturnRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long rentalOrderId;
    private Long carId;
    private Long userId;
    private Long returnRegionId;
    private String vehicleCondition;
    private BigDecimal damageCost;
    private BigDecimal lateFee;
    private BigDecimal finalAmount;
    private Long processedBy;
    private LocalDateTime returnTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
