package com.cartaxi.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleItemVO {

    private String code;
    private String name;
    private String path;
    private String description;
}
