package com.cartaxi.vo.admin.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardRecentOrderVO {

    private Long id;
    private String orderNo;
    private String orderStatus;
    private String username;
    private String realName;
    private String carCode;
    private String licensePlate;
    private BigDecimal orderAmount;
    private LocalDateTime createTime;
}