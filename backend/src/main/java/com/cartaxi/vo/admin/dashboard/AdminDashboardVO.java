package com.cartaxi.vo.admin.dashboard;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardVO {

    private Long todayOrderCount;
    private Long usingOrderCount;
    private Long pendingReturnCount;
    private Long availableCarCount;
    private Long maintenanceCarCount;
    private Long regionCount;
    private List<AdminDashboardRecentOrderVO> recentOrders;
    private List<AdminDashboardRecentReturnVO> recentReturns;
}