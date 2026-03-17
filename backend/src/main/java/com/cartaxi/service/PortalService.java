package com.cartaxi.service;

import com.cartaxi.vo.ModuleItemVO;
import java.util.List;

public interface PortalService {

    List<ModuleItemVO> getAdminModules();

    List<ModuleItemVO> getUserModules();
}
