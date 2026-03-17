CREATE DATABASE IF NOT EXISTS `cartaxi` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `cartaxi`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `return_record`;
DROP TABLE IF EXISTS `rental_order`;
DROP TABLE IF EXISTS `car_recovery_record`;
DROP TABLE IF EXISTS `car_deploy_record`;
DROP TABLE IF EXISTS `car_info`;
DROP TABLE IF EXISTS `region`;
DROP TABLE IF EXISTS `sys_admin`;
DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `id_card` VARCHAR(30) DEFAULT NULL COMMENT '身份证号',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  KEY `idx_sys_user_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `sys_admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `role_name` VARCHAR(30) NOT NULL DEFAULT 'ADMIN' COMMENT '角色',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_admin_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

CREATE TABLE `region` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `region_name` VARCHAR(100) NOT NULL COMMENT '区域名称',
  `region_code` VARCHAR(50) NOT NULL COMMENT '区域编码',
  `address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `longitude` DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_region_name` (`region_name`),
  UNIQUE KEY `uk_region_code` (`region_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域表';

CREATE TABLE `car_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `car_code` VARCHAR(50) NOT NULL COMMENT '车辆编号',
  `license_plate` VARCHAR(30) NOT NULL COMMENT '车牌号',
  `brand` VARCHAR(50) NOT NULL COMMENT '品牌',
  `model` VARCHAR(100) NOT NULL COMMENT '型号',
  `color` VARCHAR(30) DEFAULT NULL COMMENT '颜色',
  `seat_count` INT NOT NULL DEFAULT 5 COMMENT '座位数',
  `energy_type` VARCHAR(30) DEFAULT NULL COMMENT '能源类型',
  `daily_rent` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '日租金',
  `deposit` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '押金',
  `current_region_id` BIGINT DEFAULT NULL COMMENT '当前区域ID',
  `status` VARCHAR(30) NOT NULL DEFAULT 'IDLE' COMMENT '车辆状态',
  `mileage` DECIMAL(10,2) DEFAULT 0.00 COMMENT '里程',
  `battery_level` INT DEFAULT NULL COMMENT '电量百分比',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_car_info_car_code` (`car_code`),
  UNIQUE KEY `uk_car_info_license_plate` (`license_plate`),
  KEY `idx_car_info_region` (`current_region_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆信息表';

CREATE TABLE `car_deploy_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `car_id` BIGINT NOT NULL COMMENT '车辆ID',
  `region_id` BIGINT NOT NULL COMMENT '投放区域ID',
  `operator_admin_id` BIGINT NOT NULL COMMENT '操作管理员ID',
  `deploy_time` DATETIME NOT NULL COMMENT '投放时间',
  `status` VARCHAR(30) NOT NULL DEFAULT 'DEPLOYED' COMMENT '投放状态',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_car_deploy_record_car_id` (`car_id`),
  KEY `idx_car_deploy_record_region_id` (`region_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆投放记录表';

CREATE TABLE `car_recovery_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `car_id` BIGINT NOT NULL COMMENT '车辆ID',
  `region_id` BIGINT DEFAULT NULL COMMENT '回收前区域ID',
  `operator_admin_id` BIGINT NOT NULL COMMENT '操作管理员ID',
  `recovery_time` DATETIME NOT NULL COMMENT '回收时间',
  `recovery_reason` VARCHAR(255) DEFAULT NULL COMMENT '回收原因',
  `status` VARCHAR(30) NOT NULL DEFAULT 'RECOVERED' COMMENT '回收状态',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_car_recovery_record_car_id` (`car_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆回收记录表';

CREATE TABLE `rental_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `car_id` BIGINT NOT NULL COMMENT '车辆ID',
  `pickup_region_id` BIGINT NOT NULL COMMENT '取车区域ID',
  `return_region_id` BIGINT DEFAULT NULL COMMENT '还车区域ID',
  `order_status` VARCHAR(30) NOT NULL DEFAULT 'CREATED' COMMENT '订单状态',
  `order_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单金额',
  `deposit_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '押金金额',
  `start_time` DATETIME DEFAULT NULL COMMENT '租赁开始时间',
  `expected_return_time` DATETIME DEFAULT NULL COMMENT '预计归还时间',
  `actual_return_time` DATETIME DEFAULT NULL COMMENT '实际归还时间',
  `payment_status` VARCHAR(30) NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rental_order_order_no` (`order_no`),
  KEY `idx_rental_order_user_id` (`user_id`),
  KEY `idx_rental_order_car_id` (`car_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁订单表';

CREATE TABLE `return_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rental_order_id` BIGINT NOT NULL COMMENT '订单ID',
  `car_id` BIGINT NOT NULL COMMENT '车辆ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `return_region_id` BIGINT NOT NULL COMMENT '归还区域ID',
  `vehicle_condition` VARCHAR(255) DEFAULT NULL COMMENT '车辆状况',
  `damage_cost` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '损坏费用',
  `late_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '逾期费用',
  `final_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '最终结算金额',
  `processed_by` BIGINT DEFAULT NULL COMMENT '处理管理员ID',
  `return_time` DATETIME NOT NULL COMMENT '归还时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_return_record_rental_order_id` (`rental_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='归还记录表';

INSERT INTO `sys_admin` (`username`, `password`, `real_name`, `phone`, `email`, `role_name`, `status`)
VALUES ('admin', '123456', '系统管理员', '13800000000', 'admin@cartaxi.com', 'SUPER_ADMIN', 1);

INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `status`)
VALUES ('user01', '123456', '演示用户', '13900000000', 'user01@cartaxi.com', 1);

INSERT INTO `region` (`region_name`, `region_code`, `address`, `longitude`, `latitude`, `status`, `remark`)
VALUES
('浦东投放点', 'PD001', '上海市浦东新区示例地址 1 号', 121.544346, 31.221461, 1, '默认投放区域'),
('虹桥投放点', 'HQ001', '上海市闵行区示例地址 2 号', 121.318225, 31.197646, 1, '默认还车区域');

INSERT INTO `car_info` (`car_code`, `license_plate`, `brand`, `model`, `color`, `seat_count`, `energy_type`, `daily_rent`, `deposit`, `current_region_id`, `status`, `mileage`, `battery_level`, `remark`)
VALUES
('CAR0001', '沪A12345', '比亚迪', '秦PLUS EV', '白色', 5, 'EV', 168.00, 1000.00, 1, 'DEPLOYED', 1200.50, 86, '演示车辆'),
('CAR0002', '沪B67890', '特斯拉', 'Model 3', '黑色', 5, 'EV', 298.00, 2000.00, 2, 'IDLE', 850.00, 72, '演示车辆');

SET FOREIGN_KEY_CHECKS = 1;
