package com.cartaxi.dto.user.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserProfileUpdateDTO {

    @NotBlank(message = "请输入真实姓名")
    private String realName;

    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1\\d{10}$", message = "请输入正确的手机号")
    private String phone;

    @NotBlank(message = "请输入邮箱")
    @Email(message = "请输入正确的邮箱地址")
    private String email;

    private String idCard;
    private String avatarUrl;
}