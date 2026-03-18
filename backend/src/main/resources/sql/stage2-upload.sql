ALTER TABLE car_info
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(255) NULL COMMENT '车辆图片地址' AFTER battery_level;
