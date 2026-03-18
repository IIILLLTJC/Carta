package com.cartaxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cartaxi.common.context.UserContext;
import com.cartaxi.common.exception.BusinessException;
import com.cartaxi.entity.CarInfo;
import com.cartaxi.entity.Region;
import com.cartaxi.entity.RentalOrder;
import com.cartaxi.entity.ReturnRecord;
import com.cartaxi.entity.SysUser;
import com.cartaxi.mapper.CarInfoMapper;
import com.cartaxi.mapper.RegionMapper;
import com.cartaxi.mapper.RentalOrderMapper;
import com.cartaxi.mapper.ReturnRecordMapper;
import com.cartaxi.mapper.SysUserMapper;
import com.cartaxi.service.PortalService;
import com.cartaxi.vo.ModuleItemVO;
import com.cartaxi.vo.admin.dashboard.AdminDashboardRecentOrderVO;
import com.cartaxi.vo.admin.dashboard.AdminDashboardRecentReturnVO;
import com.cartaxi.vo.admin.dashboard.AdminDashboardVO;
import com.cartaxi.vo.user.dashboard.UserDashboardVO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PortalServiceImpl implements PortalService {

    private final RentalOrderMapper rentalOrderMapper;
    private final ReturnRecordMapper returnRecordMapper;
    private final CarInfoMapper carInfoMapper;
    private final RegionMapper regionMapper;
    private final SysUserMapper sysUserMapper;

    public PortalServiceImpl(RentalOrderMapper rentalOrderMapper,
                             ReturnRecordMapper returnRecordMapper,
                             CarInfoMapper carInfoMapper,
                             RegionMapper regionMapper,
                             SysUserMapper sysUserMapper) {
        this.rentalOrderMapper = rentalOrderMapper;
        this.returnRecordMapper = returnRecordMapper;
        this.carInfoMapper = carInfoMapper;
        this.regionMapper = regionMapper;
        this.sysUserMapper = sysUserMapper;
    }

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

    @Override
    public AdminDashboardVO getAdminDashboard() {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime nextDayStart = dayStart.plusDays(1);

        long todayOrderCount = rentalOrderMapper.selectCount(new LambdaQueryWrapper<RentalOrder>()
                .ge(RentalOrder::getCreateTime, dayStart)
                .lt(RentalOrder::getCreateTime, nextDayStart));
        long usingOrderCount = rentalOrderMapper.selectCount(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getOrderStatus, "USING"));
        long pendingReturnCount = returnRecordMapper.selectCount(new LambdaQueryWrapper<ReturnRecord>()
                .eq(ReturnRecord::getStatus, "PENDING"));
        long availableCarCount = carInfoMapper.selectCount(new LambdaQueryWrapper<CarInfo>()
                .eq(CarInfo::getStatus, "DEPLOYED"));
        long maintenanceCarCount = carInfoMapper.selectCount(new LambdaQueryWrapper<CarInfo>()
                .eq(CarInfo::getStatus, "MAINTENANCE"));
        long regionCount = regionMapper.selectCount(new LambdaQueryWrapper<Region>());

        List<RentalOrder> recentOrders = rentalOrderMapper.selectList(new LambdaQueryWrapper<RentalOrder>()
                .orderByDesc(RentalOrder::getCreateTime)
                .last("limit 6"));
        List<ReturnRecord> recentReturns = returnRecordMapper.selectList(new LambdaQueryWrapper<ReturnRecord>()
                .orderByDesc(ReturnRecord::getReturnTime)
                .orderByDesc(ReturnRecord::getCreateTime)
                .last("limit 6"));

        return AdminDashboardVO.builder()
                .todayOrderCount(todayOrderCount)
                .usingOrderCount(usingOrderCount)
                .pendingReturnCount(pendingReturnCount)
                .availableCarCount(availableCarCount)
                .maintenanceCarCount(maintenanceCarCount)
                .regionCount(regionCount)
                .recentOrders(toAdminRecentOrders(recentOrders))
                .recentReturns(toAdminRecentReturns(recentReturns))
                .build();
    }

    @Override
    public UserDashboardVO getUserDashboard() {
        Long userId = currentUserId();
        long activeOrderCount = rentalOrderMapper.selectCount(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getUserId, userId)
                .in(RentalOrder::getOrderStatus, List.of("USING", "RETURNING")));
        long pendingReturnCount = returnRecordMapper.selectCount(new LambdaQueryWrapper<ReturnRecord>()
                .eq(ReturnRecord::getUserId, userId)
                .eq(ReturnRecord::getStatus, "PENDING"));
        long historyOrderCount = rentalOrderMapper.selectCount(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getUserId, userId)
                .in(RentalOrder::getOrderStatus, List.of("COMPLETED", "CANCELLED")));
        List<ModuleItemVO> quickEntries = getUserModules().stream()
                .filter(item -> !"/user/home".equals(item.getPath()))
                .toList();

        return UserDashboardVO.builder()
                .activeOrderCount(activeOrderCount)
                .pendingReturnCount(pendingReturnCount)
                .historyOrderCount(historyOrderCount)
                .quickEntries(quickEntries)
                .build();
    }

    private List<AdminDashboardRecentOrderVO> toAdminRecentOrders(List<RentalOrder> orders) {
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, SysUser> userMap = mapUsers(orders.stream().map(RentalOrder::getUserId).filter(Objects::nonNull).distinct().toList());
        Map<Long, CarInfo> carMap = mapCars(orders.stream().map(RentalOrder::getCarId).filter(Objects::nonNull).distinct().toList());
        return orders.stream().map(order -> {
            SysUser user = userMap.get(order.getUserId());
            CarInfo car = carMap.get(order.getCarId());
            return AdminDashboardRecentOrderVO.builder()
                    .id(order.getId())
                    .orderNo(order.getOrderNo())
                    .orderStatus(order.getOrderStatus())
                    .username(user == null ? null : user.getUsername())
                    .realName(user == null ? null : user.getRealName())
                    .carCode(car == null ? null : car.getCarCode())
                    .licensePlate(car == null ? null : car.getLicensePlate())
                    .orderAmount(order.getOrderAmount())
                    .createTime(order.getCreateTime())
                    .build();
        }).toList();
    }

    private List<AdminDashboardRecentReturnVO> toAdminRecentReturns(List<ReturnRecord> records) {
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, RentalOrder> orderMap = mapOrders(records.stream().map(ReturnRecord::getRentalOrderId).filter(Objects::nonNull).distinct().toList());
        Map<Long, SysUser> userMap = mapUsers(records.stream().map(ReturnRecord::getUserId).filter(Objects::nonNull).distinct().toList());
        Map<Long, CarInfo> carMap = mapCars(records.stream().map(ReturnRecord::getCarId).filter(Objects::nonNull).distinct().toList());
        return records.stream().map(record -> {
            RentalOrder order = orderMap.get(record.getRentalOrderId());
            SysUser user = userMap.get(record.getUserId());
            CarInfo car = carMap.get(record.getCarId());
            return AdminDashboardRecentReturnVO.builder()
                    .id(record.getId())
                    .orderNo(order == null ? null : order.getOrderNo())
                    .status(record.getStatus())
                    .username(user == null ? null : user.getUsername())
                    .realName(user == null ? null : user.getRealName())
                    .carCode(car == null ? null : car.getCarCode())
                    .licensePlate(car == null ? null : car.getLicensePlate())
                    .vehicleCondition(record.getVehicleCondition())
                    .returnTime(record.getReturnTime())
                    .build();
        }).toList();
    }

    private Map<Long, RentalOrder> mapOrders(List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return rentalOrderMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(RentalOrder::getId, item -> item));
    }

    private Map<Long, CarInfo> mapCars(List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return carInfoMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(CarInfo::getId, item -> item));
    }

    private Map<Long, SysUser> mapUsers(List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysUserMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(SysUser::getId, item -> item));
    }

    private Long currentUserId() {
        if (UserContext.get() == null || UserContext.get().getUserId() == null) {
            throw new BusinessException(401, "当前用户未登录");
        }
        return UserContext.get().getUserId();
    }
}