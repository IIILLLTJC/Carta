package com.cartaxi.controller.user;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.dto.user.profile.UserProfileUpdateDTO;
import com.cartaxi.service.PortalService;
import com.cartaxi.service.UserProfileService;
import com.cartaxi.vo.user.dashboard.UserDashboardVO;
import com.cartaxi.vo.user.profile.UserProfileVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@LoginRequired(roles = {"USER"})
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final PortalService portalService;

    public UserProfileController(UserProfileService userProfileService,
                                 PortalService portalService) {
        this.userProfileService = userProfileService;
        this.portalService = portalService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<UserDashboardVO> dashboard() {
        return ApiResponse.success("查询成功", portalService.getUserDashboard());
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileVO> profile() {
        return ApiResponse.success("查询成功", userProfileService.getCurrentProfile());
    }

    @PutMapping("/profile")
    public ApiResponse<UserProfileVO> update(@Valid @RequestBody UserProfileUpdateDTO updateDTO) {
        return ApiResponse.success("更新成功", userProfileService.updateCurrentProfile(updateDTO));
    }
}