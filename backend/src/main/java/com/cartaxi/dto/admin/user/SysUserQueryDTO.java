package com.cartaxi.dto.admin.user;

import lombok.Data;

@Data
public class SysUserQueryDTO {

    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String username;
    private String realName;
    private String phone;
    private Integer status;
}