package com.cartaxi.dto.admin.admin;

import lombok.Data;

@Data
public class SysAdminQueryDTO {

    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String username;
    private String realName;
    private String phone;
    private String roleName;
    private Integer status;
}