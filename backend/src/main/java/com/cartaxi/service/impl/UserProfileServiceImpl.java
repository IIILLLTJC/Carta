package com.cartaxi.service.impl;

import com.cartaxi.common.context.UserContext;
import com.cartaxi.common.exception.BusinessException;
import com.cartaxi.dto.user.profile.UserProfileUpdateDTO;
import com.cartaxi.entity.SysUser;
import com.cartaxi.mapper.SysUserMapper;
import com.cartaxi.service.UserProfileService;
import com.cartaxi.vo.user.profile.UserProfileVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final SysUserMapper sysUserMapper;

    public UserProfileServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public UserProfileVO getCurrentProfile() {
        return toVO(getCurrentUser());
    }

    @Override
    public UserProfileVO updateCurrentProfile(UserProfileUpdateDTO updateDTO) {
        SysUser user = getCurrentUser();
        user.setRealName(updateDTO.getRealName().trim());
        user.setPhone(updateDTO.getPhone().trim());
        user.setEmail(updateDTO.getEmail().trim());
        user.setIdCard(StringUtils.hasText(updateDTO.getIdCard()) ? updateDTO.getIdCard().trim() : null);
        user.setAvatarUrl(StringUtils.hasText(updateDTO.getAvatarUrl()) ? updateDTO.getAvatarUrl().trim() : null);
        sysUserMapper.updateById(user);
        return toVO(sysUserMapper.selectById(user.getId()));
    }

    private SysUser getCurrentUser() {
        if (UserContext.get() == null || UserContext.get().getUserId() == null) {
            throw new BusinessException(401, "???????????");
        }
        SysUser user = sysUserMapper.selectById(UserContext.get().getUserId());
        if (user == null) {
            throw new BusinessException(404, "???????");
        }
        return user;
    }

    private UserProfileVO toVO(SysUser user) {
        return UserProfileVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .idCard(user.getIdCard())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .build();
    }
}
