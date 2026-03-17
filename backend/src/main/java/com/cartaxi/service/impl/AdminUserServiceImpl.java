package com.cartaxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cartaxi.common.exception.BusinessException;
import com.cartaxi.dto.admin.user.SysUserQueryDTO;
import com.cartaxi.dto.admin.user.SysUserSaveDTO;
import com.cartaxi.entity.SysUser;
import com.cartaxi.mapper.SysUserMapper;
import com.cartaxi.service.AdminUserService;
import com.cartaxi.vo.admin.user.SysUserVO;
import com.cartaxi.vo.common.PageResult;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final SysUserMapper sysUserMapper;

    public AdminUserServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public PageResult<SysUserVO> page(SysUserQueryDTO queryDTO) {
        Page<SysUser> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getUsername()), SysUser::getUsername, queryDTO.getUsername())
                .like(StringUtils.hasText(queryDTO.getRealName()), SysUser::getRealName, queryDTO.getRealName())
                .like(StringUtils.hasText(queryDTO.getPhone()), SysUser::getPhone, queryDTO.getPhone())
                .eq(queryDTO.getStatus() != null, SysUser::getStatus, queryDTO.getStatus())
                .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = sysUserMapper.selectPage(page, wrapper);
        List<SysUserVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public SysUserVO detail(Long id) {
        return toVO(getUserOrThrow(id));
    }

    @Override
    public void create(SysUserSaveDTO saveDTO) {
        validateStatus(saveDTO.getStatus());
        if (!StringUtils.hasText(saveDTO.getPassword())) {
            throw new BusinessException(400, "新增用户时密码不能为空");
        }
        ensureUsernameUnique(null, saveDTO.getUsername());
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(saveDTO, sysUser);
        sysUserMapper.insert(sysUser);
    }

    @Override
    public void update(Long id, SysUserSaveDTO saveDTO) {
        SysUser sysUser = getUserOrThrow(id);
        validateStatus(saveDTO.getStatus());
        ensureUsernameUnique(id, saveDTO.getUsername());
        String originalPassword = sysUser.getPassword();
        BeanUtils.copyProperties(saveDTO, sysUser);
        if (!StringUtils.hasText(saveDTO.getPassword())) {
            sysUser.setPassword(originalPassword);
        }
        sysUserMapper.updateById(sysUser);
    }

    @Override
    public void delete(Long id) {
        getUserOrThrow(id);
        sysUserMapper.deleteById(id);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        SysUser sysUser = getUserOrThrow(id);
        validateStatus(status);
        sysUser.setStatus(status);
        sysUserMapper.updateById(sysUser);
    }

    private SysUser getUserOrThrow(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if (sysUser == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return sysUser;
    }

    private void ensureUsernameUnique(Long id, String username) {
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        boolean duplicated = users.stream().anyMatch(item -> !Objects.equals(item.getId(), id));
        if (duplicated) {
            throw new BusinessException(409, "用户名已存在");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "用户状态仅支持 0 或 1");
        }
    }

    private SysUserVO toVO(SysUser sysUser) {
        return SysUserVO.builder()
                .id(sysUser.getId())
                .username(sysUser.getUsername())
                .realName(sysUser.getRealName())
                .phone(sysUser.getPhone())
                .email(sysUser.getEmail())
                .idCard(sysUser.getIdCard())
                .avatarUrl(sysUser.getAvatarUrl())
                .status(sysUser.getStatus())
                .createTime(sysUser.getCreateTime())
                .updateTime(sysUser.getUpdateTime())
                .build();
    }
}