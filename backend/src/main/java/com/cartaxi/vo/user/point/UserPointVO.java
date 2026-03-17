package com.cartaxi.vo.user.point;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointVO {

    private Long id;
    private String regionName;
    private String regionCode;
    private String address;
    private Double longitude;
    private Double latitude;
    private Integer availableCarCount;
    private List<UserPointCarVO> availableCars;
}