package com.cartaxi.vo.user.returnrecord;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserReturnableOrderVO {

    private Long id;
    private String orderNo;
    private Long carId;
    private String carCode;
    private String licensePlate;
    private String brand;
    private String model;
    private Long pickupRegionId;
    private String pickupRegionName;
    private Long returnRegionId;
    private String returnRegionName;
    private String orderStatus;
    private LocalDateTime startTime;
    private LocalDateTime expectedReturnTime;
    private String remark;
}
