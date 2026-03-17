package com.cartaxi.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String token;
    private String tokenPrefix;
    private Long expiresIn;
    private String role;
    private String displayName;
}
