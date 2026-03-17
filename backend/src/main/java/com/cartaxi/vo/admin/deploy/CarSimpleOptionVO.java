package com.cartaxi.vo.admin.deploy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarSimpleOptionVO {

    private Long id;
    private String carCode;
    private String licensePlate;
    private String displayName;
}