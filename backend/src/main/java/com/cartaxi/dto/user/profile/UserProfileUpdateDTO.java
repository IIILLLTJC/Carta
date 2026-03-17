package com.cartaxi.dto.user.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserProfileUpdateDTO {

    @NotBlank(message = "??????")
    private String realName;

    @NotBlank(message = "???????")
    @Pattern(regexp = "^1\\d{10}$", message = "????????")
    private String phone;

    @NotBlank(message = "??????")
    @Email(message = "???????")
    private String email;

    private String idCard;
    private String avatarUrl;
}
