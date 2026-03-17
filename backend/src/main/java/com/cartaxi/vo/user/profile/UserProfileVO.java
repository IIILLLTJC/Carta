package com.cartaxi.vo.user.profile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileVO {

    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String idCard;
    private String avatarUrl;
    private Integer status;
}
