package com.cartaxi.vo.admin.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSimpleOptionVO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String displayName;
}