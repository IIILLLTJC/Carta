package com.cartaxi.vo.admin.region;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionOptionVO {

    private Long id;
    private String regionName;
    private String regionCode;
}