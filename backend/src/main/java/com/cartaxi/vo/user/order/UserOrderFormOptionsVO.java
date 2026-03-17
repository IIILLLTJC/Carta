package com.cartaxi.vo.user.order;

import com.cartaxi.vo.admin.deploy.CarSimpleOptionVO;
import com.cartaxi.vo.admin.region.RegionOptionVO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOrderFormOptionsVO {

    private List<CarSimpleOptionVO> cars;
    private List<RegionOptionVO> regions;
}