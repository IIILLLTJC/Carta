package com.cartaxi.dto.admin.region;

import lombok.Data;

@Data
public class RegionQueryDTO {

    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String regionName;
    private String regionCode;
    private Integer status;
}