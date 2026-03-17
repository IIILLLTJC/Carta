package com.cartaxi.vo.user.returnrecord;

import com.cartaxi.vo.admin.region.RegionOptionVO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserReturnFormOptionsVO {

    private List<UserReturnableOrderVO> orders;
    private List<RegionOptionVO> regions;
}
