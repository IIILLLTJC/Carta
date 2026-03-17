package com.cartaxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("car_recovery_record")
public class CarRecoveryRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long carId;
    private Long regionId;
    private Long operatorAdminId;
    private LocalDateTime recoveryTime;
    private String recoveryReason;
    private String status;
    private String remark;
    private LocalDateTime createTime;
}
