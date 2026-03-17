package com.cartaxi.vo.admin.recovery;

import com.cartaxi.vo.admin.deploy.AdminSimpleOptionVO;
import com.cartaxi.vo.admin.deploy.CarSimpleOptionVO;
import com.cartaxi.vo.admin.region.RegionOptionVO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarRecoveryFormOptionsVO {

    private List<CarSimpleOptionVO> cars;
    private List<RegionOptionVO> regions;
    private List<AdminSimpleOptionVO> admins;
}