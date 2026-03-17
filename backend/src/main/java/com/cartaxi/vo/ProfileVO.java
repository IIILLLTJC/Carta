package com.cartaxi.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileVO {

    private Long userId;
    private String username;
    private String displayName;
    private String role;
    private String phone;
    private String email;
}
