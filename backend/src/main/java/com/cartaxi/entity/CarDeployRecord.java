package com.cartaxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("car_deploy_record")
public class CarDeployRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long carId;
    private Long regionId;
    private Long operatorAdminId;
    private LocalDateTime deployTime;
    private String status;
    private String remark;
    private LocalDateTime createTime;
}
