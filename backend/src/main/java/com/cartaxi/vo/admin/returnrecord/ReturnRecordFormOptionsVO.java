package com.cartaxi.vo.admin.returnrecord;

import com.cartaxi.vo.admin.deploy.AdminSimpleOptionVO;
import com.cartaxi.vo.admin.region.RegionOptionVO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRecordFormOptionsVO {

    private List<ReturnOrderOptionVO> orders;
    private List<RegionOptionVO> regions;
    private List<AdminSimpleOptionVO> admins;
}