package com.cartaxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cartaxi.common.enums.RoleType;
import com.cartaxi.common.exception.BusinessException;
import com.cartaxi.dto.admin.admin.SysAdminQueryDTO;
import com.cartaxi.dto.admin.admin.SysAdminSaveDTO;
import com.cartaxi.entity.SysAdmin;
import com.cartaxi.mapper.SysAdminMapper;
import com.cartaxi.service.AdminSysAdminService;
import com.cartaxi.vo.admin.admin.SysAdminVO;
import com.cartaxi.vo.common.PageResult;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminSysAdminServiceImpl implements AdminSysAdminService {

    private static final Set<String> ALLOWED_ROLES = Set.of(RoleType.ADMIN.name(), RoleType.SUPER_ADMIN.name());

    private final SysAdminMapper sysAdminMapper;

    public AdminSysAdminServiceImpl(SysAdminMapper sysAdminMapper) {
        this.sysAdminMapper = sysAdminMapper;
    }

    @Override
    public PageResult<SysAdminVO> page(SysAdminQueryDTO queryDTO) {
        Page<SysAdmin> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SysAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getUsername()), SysAdmin::getUsername, queryDTO.getUsername())
                .like(StringUtils.hasText(queryDTO.getRealName()), SysAdmin::getRealName, queryDTO.getRealName())
                .like(StringUtils.hasText(queryDTO.getPhone()), SysAdmin::getPhone, queryDTO.getPhone())
                .eq(StringUtils.hasText(queryDTO.getRoleName()), SysAdmin::getRoleName, queryDTO.getRoleName())
                .eq(queryDTO.getStatus() != null, SysAdmin::getStatus, queryDTO.getStatus())
                .orderByDesc(SysAdmin::getCreateTime);
        Page<SysAdmin> result = sysAdminMapper.selectPage(page, wrapper);
        List<SysAdminVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public SysAdminVO detail(Long id) {
        return toVO(getAdminOrThrow(id));
    }

    @Override
    public void create(SysAdminSaveDTO saveDTO) {
        validateStatus(saveDTO.getStatus());
        validateRole(saveDTO.getRoleName());
        if (!StringUtils.hasText(saveDTO.getPassword())) {
            throw new BusinessException(400, "新增管理员时密码不能为空");
        }
        ensureUsernameUnique(null, saveDTO.getUsername());
        SysAdmin sysAdmin = new SysAdmin();
        BeanUtils.copyProperties(saveDTO, sysAdmin);
        sysAdminMapper.insert(sysAdmin);
    }

    @Override
    public void update(Long id, SysAdminSaveDTO saveDTO) {
        SysAdmin sysAdmin = getAdminOrThrow(id);
        validateStatus(saveDTO.getStatus());
        validateRole(saveDTO.getRoleName());
        ensureUsernameUnique(id, saveDTO.getUsername());
        String originalPassword = sysAdmin.getPassword();
        BeanUtils.copyProperties(saveDTO, sysAdmin);
        if (!StringUtils.hasText(saveDTO.getPassword())) {
            sysAdmin.setPassword(originalPassword);
        }
        sysAdminMapper.updateById(sysAdmin);
    }

    @Override
    public void delete(Long id) {
        getAdminOrThrow(id);
        sysAdminMapper.deleteById(id);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        SysAdmin sysAdmin = getAdminOrThrow(id);
        validateStatus(status);
        sysAdmin.setStatus(status);
        sysAdminMapper.updateById(sysAdmin);
    }

    private SysAdmin getAdminOrThrow(Long id) {
        SysAdmin sysAdmin = sysAdminMapper.selectById(id);
        if (sysAdmin == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        return sysAdmin;
    }

    private void ensureUsernameUnique(Long id, String username) {
        List<SysAdmin> admins = sysAdminMapper.selectList(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getUsername, username));
        boolean duplicated = admins.stream().anyMatch(item -> !Objects.equals(item.getId(), id));
        if (duplicated) {
            throw new BusinessException(409, "用户名已存在");
        }
    }

    private void validateRole(String roleName) {
        if (!StringUtils.hasText(roleName) || !ALLOWED_ROLES.contains(roleName)) {
            throw new BusinessException(400, "管理员角色仅支持 ADMIN 或 SUPER_ADMIN");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "管理员状态仅支持 0 或 1");
        }
    }

    private SysAdminVO toVO(SysAdmin sysAdmin) {
        return SysAdminVO.builder()
                .id(sysAdmin.getId())
                .username(sysAdmin.getUsername())
                .realName(sysAdmin.getRealName())
                .phone(sysAdmin.getPhone())
                .email(sysAdmin.getEmail())
                .roleName(sysAdmin.getRoleName())
                .status(sysAdmin.getStatus())
                .createTime(sysAdmin.getCreateTime())
                .updateTime(sysAdmin.getUpdateTime())
                .build();
    }
}