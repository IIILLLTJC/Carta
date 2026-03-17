package com.cartaxi.dto.user.point;

import lombok.Data;

@Data
public class UserPointQueryDTO {

    private Long pageNum = 1L;
    private Long pageSize = 6L;
    private String regionName;
}