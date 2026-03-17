package com.cartaxi.dto.admin.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SysAdminSaveDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50")
    private String username;

    @Size(max = 100, message = "密码长度不能超过100")
    private String password;

    @Size(max = 50, message = "真实姓名长度不能超过50")
    private String realName;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    @NotBlank(message = "角色不能为空")
    private String roleName;

    @NotNull(message = "状态不能为空")
    private Integer status;
}