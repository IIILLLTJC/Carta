package com.cartaxi.dto.admin.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SysUserSaveDTO {

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

    @Size(max = 30, message = "身份证号长度不能超过30")
    private String idCard;

    @Size(max = 255, message = "头像地址长度不能超过255")
    private String avatarUrl;

    @NotNull(message = "状态不能为空")
    private Integer status;
}