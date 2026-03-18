package com.cartaxi.vo.admin.dashboard;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardRecentReturnVO {

    private Long id;
    private String orderNo;
    private String status;
    private String username;
    private String realName;
    private String carCode;
    private String licensePlate;
    private String vehicleCondition;
    private LocalDateTime returnTime;
}