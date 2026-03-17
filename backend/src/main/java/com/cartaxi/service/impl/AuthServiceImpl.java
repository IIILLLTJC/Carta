package com.cartaxi.service.impl;

import com.cartaxi.common.context.UserContext;
import com.cartaxi.common.enums.RoleType;
import com.cartaxi.common.exception.BusinessException;
import com.cartaxi.common.model.LoginUser;
import com.cartaxi.common.util.JwtTokenUtil;
import com.cartaxi.dto.LoginRequest;
import com.cartaxi.service.AuthService;
import com.cartaxi.vo.LoginVO;
import com.cartaxi.vo.ProfileVO;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final JwtTokenUtil jwtTokenUtil;

    public AuthServiceImpl(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public LoginVO userLogin(LoginRequest request) {
        if (!"user01".equals(request.getUsername()) || !"123456".equals(request.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        LoginUser loginUser = LoginUser.builder()
                .userId(1L)
                .username(request.getUsername())
                .displayName("演示用户")
                .role(RoleType.USER.name())
                .build();
        return buildLoginVO(loginUser);
    }

    @Override
    public LoginVO adminLogin(LoginRequest request) {
        if (!"admin".equals(request.getUsername()) || !"123456".equals(request.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        LoginUser loginUser = LoginUser.builder()
                .userId(1001L)
                .username(request.getUsername())
                .displayName("系统管理员")
                .role(RoleType.SUPER_ADMIN.name())
                .build();
        return buildLoginVO(loginUser);
    }

    @Override
    public ProfileVO getProfile() {
        LoginUser loginUser = UserContext.get();
        if (loginUser == null) {
            throw new BusinessException(401, "当前用户未登录");
        }
        return ProfileVO.builder()
                .userId(loginUser.getUserId())
                .username(loginUser.getUsername())
                .displayName(loginUser.getDisplayName())
                .role(loginUser.getRole())
                .phone(loginUser.getRole().equals(RoleType.USER.name()) ? "13900000000" : "13800000000")
                .email(loginUser.getRole().equals(RoleType.USER.name()) ? "user01@cartaxi.com" : "admin@cartaxi.com")
                .build();
    }

    private LoginVO buildLoginVO(LoginUser loginUser) {
        return LoginVO.builder()
                .token(jwtTokenUtil.generateToken(loginUser))
                .tokenPrefix("Bearer")
                .expiresIn(jwtTokenUtil.getExpireSeconds())
                .role(loginUser.getRole())
                .displayName(loginUser.getDisplayName())
                .build();
    }
}
