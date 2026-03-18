package com.cartaxi.service;

import com.cartaxi.vo.ModuleItemVO;
import com.cartaxi.vo.admin.dashboard.AdminDashboardVO;
import com.cartaxi.vo.user.dashboard.UserDashboardVO;
import java.util.List;

public interface PortalService {

    List<ModuleItemVO> getAdminModules();

    List<ModuleItemVO> getUserModules();

    AdminDashboardVO getAdminDashboard();

    UserDashboardVO getUserDashboard();
}