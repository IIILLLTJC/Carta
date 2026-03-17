package com.cartaxi.controller.user;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.dto.user.profile.UserProfileUpdateDTO;
import com.cartaxi.service.UserProfileService;
import com.cartaxi.vo.user.profile.UserProfileVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/profile")
@LoginRequired(roles = {"USER"})
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ApiResponse<UserProfileVO> profile() {
        return ApiResponse.success("????", userProfileService.getCurrentProfile());
    }

    @PutMapping
    public ApiResponse<UserProfileVO> update(@Valid @RequestBody UserProfileUpdateDTO updateDTO) {
        return ApiResponse.success("????", userProfileService.updateCurrentProfile(updateDTO));
    }
}
