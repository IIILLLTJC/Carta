package com.cartaxi.controller;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.dto.LoginRequest;
import com.cartaxi.service.AuthService;
import com.cartaxi.vo.LoginVO;
import com.cartaxi.vo.ProfileVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/user/login")
    public ApiResponse<LoginVO> userLogin(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.userLogin(request));
    }

    @PostMapping("/admin/login")
    public ApiResponse<LoginVO> adminLogin(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.adminLogin(request));
    }

    @LoginRequired
    @GetMapping("/profile")
    public ApiResponse<ProfileVO> getProfile() {
        return ApiResponse.success("查询成功", authService.getProfile());
    }
}
