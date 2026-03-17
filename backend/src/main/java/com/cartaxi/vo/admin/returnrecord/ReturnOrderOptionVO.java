package com.cartaxi.vo.admin.returnrecord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnOrderOptionVO {

    private Long id;
    private String orderNo;
    private String displayName;
}