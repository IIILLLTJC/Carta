package com.cartaxi.vo.user.dashboard;

import com.cartaxi.vo.ModuleItemVO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardVO {

    private Long activeOrderCount;
    private Long pendingReturnCount;
    private Long historyOrderCount;
    private List<ModuleItemVO> quickEntries;
}