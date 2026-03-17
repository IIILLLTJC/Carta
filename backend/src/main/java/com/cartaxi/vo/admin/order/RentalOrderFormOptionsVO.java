package com.cartaxi.vo.admin.order;

import com.cartaxi.vo.admin.deploy.CarSimpleOptionVO;
import com.cartaxi.vo.admin.region.RegionOptionVO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalOrderFormOptionsVO {

    private List<UserSimpleOptionVO> users;
    private List<CarSimpleOptionVO> cars;
    private List<RegionOptionVO> regions;
}