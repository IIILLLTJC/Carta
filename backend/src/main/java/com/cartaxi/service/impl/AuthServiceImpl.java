package com.cartaxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cartaxi.common.context.UserContext;
import com.cartaxi.common.enums.RoleType;
import com.cartaxi.common.exception.BusinessException;
import com.cartaxi.common.model.LoginUser;
import com.cartaxi.common.util.JwtTokenUtil;
import com.cartaxi.dto.LoginRequest;
import com.cartaxi.entity.SysAdmin;
import com.cartaxi.entity.SysUser;
import com.cartaxi.mapper.SysAdminMapper;
import com.cartaxi.mapper.SysUserMapper;
import com.cartaxi.service.AuthService;
import com.cartaxi.vo.LoginVO;
import com.cartaxi.vo.ProfileVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

    private final JwtTokenUtil jwtTokenUtil;
    private final SysUserMapper sysUserMapper;
    private final SysAdminMapper sysAdminMapper;

    public AuthServiceImpl(JwtTokenUtil jwtTokenUtil, SysUserMapper sysUserMapper, SysAdminMapper sysAdminMapper) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.sysUserMapper = sysUserMapper;
        this.sysAdminMapper = sysAdminMapper;
    }

    @Override
    public LoginVO userLogin(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .last("limit 1"));
        if (user == null || !request.getPassword().equals(user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(403, "当前用户已被禁用");
        }
        LoginUser loginUser = LoginUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .displayName(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername())
                .role(RoleType.USER.name())
                .build();
        return buildLoginVO(loginUser);
    }

    @Override
    public LoginVO adminLogin(LoginRequest request) {
        SysAdmin admin = sysAdminMapper.selectOne(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getUsername, request.getUsername())
                .last("limit 1"));
        if (admin == null || !request.getPassword().equals(admin.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(admin.getStatus())) {
            throw new BusinessException(403, "当前管理员已被禁用");
        }
        LoginUser loginUser = LoginUser.builder()
                .userId(admin.getId())
                .username(admin.getUsername())
                .displayName(StringUtils.hasText(admin.getRealName()) ? admin.getRealName() : admin.getUsername())
                .role(RoleType.SUPER_ADMIN.name().equals(admin.getRoleName()) ? RoleType.SUPER_ADMIN.name() : RoleType.ADMIN.name())
                .build();
        return buildLoginVO(loginUser);
    }

    @Override
    public ProfileVO getProfile() {
        LoginUser loginUser = UserContext.get();
        if (loginUser == null) {
            throw new BusinessException(401, "当前用户未登录");
        }
        if (RoleType.USER.name().equals(loginUser.getRole())) {
            SysUser user = sysUserMapper.selectById(loginUser.getUserId());
            if (user == null) {
                throw new BusinessException(404, "用户不存在");
            }
            return ProfileVO.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .displayName(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername())
                    .role(loginUser.getRole())
                    .phone(user.getPhone())
                    .email(user.getEmail())
                    .build();
        }
        SysAdmin admin = sysAdminMapper.selectById(loginUser.getUserId());
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        return ProfileVO.builder()
                .userId(admin.getId())
                .username(admin.getUsername())
                .displayName(StringUtils.hasText(admin.getRealName()) ? admin.getRealName() : admin.getUsername())
                .role(loginUser.getRole())
                .phone(admin.getPhone())
                .email(admin.getEmail())
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