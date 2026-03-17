package com.cartaxi.service.impl;

import com.cartaxi.service.PortalService;
import com.cartaxi.vo.ModuleItemVO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PortalServiceImpl implements PortalService {

    @Override
    public List<ModuleItemVO> getAdminModules() {
        return List.of(
                new ModuleItemVO("admin-profile", "个人中心", "/admin/profile", "查看管理员资料与账号信息"),
                new ModuleItemVO("admin-users", "用户管理", "/admin/users", "管理系统用户列表与状态"),
                new ModuleItemVO("admin-regions", "区域管理", "/admin/regions", "维护投放区域与地址信息"),
                new ModuleItemVO("admin-cars", "车辆信息管理", "/admin/cars", "维护车辆基础档案"),
                new ModuleItemVO("admin-deploy", "车辆投放管理", "/admin/deploy", "记录车辆投放"),
                new ModuleItemVO("admin-recovery", "车辆回收管理", "/admin/recovery", "记录车辆回收"),
                new ModuleItemVO("admin-orders", "订单中心", "/admin/orders", "查看与处理订单"),
                new ModuleItemVO("admin-returns", "归还管理", "/admin/returns", "确认归还与费用结算"),
                new ModuleItemVO("admin-admins", "管理员管理", "/admin/admins", "维护管理员账号与角色")
        );
    }

    @Override
    public List<ModuleItemVO> getUserModules() {
        return List.of(
                new ModuleItemVO("user-profile", "个人中心", "/user/profile", "查看个人资料与账号信息"),
                new ModuleItemVO("user-points", "车辆投放地点", "/user/points", "查看可租区域与投放点"),
                new ModuleItemVO("user-orders", "订单管理", "/user/orders", "查看租赁订单与状态"),
                new ModuleItemVO("user-returns", "归还管理", "/user/returns", "提交归还与查看归还记录")
        );
    }
}
