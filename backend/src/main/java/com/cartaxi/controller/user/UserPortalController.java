package com.cartaxi.controller.user;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.service.PortalService;
import com.cartaxi.vo.ModuleItemVO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@LoginRequired(roles = {"USER"})
public class UserPortalController {

    private final PortalService portalService;

    public UserPortalController(PortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/modules")
    public ApiResponse<List<ModuleItemVO>> modules() {
        return ApiResponse.success("查询成功", portalService.getUserModules());
    }
}
