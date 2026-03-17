package com.cartaxi.controller.admin;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.dto.admin.user.SysUserQueryDTO;
import com.cartaxi.dto.admin.user.SysUserSaveDTO;
import com.cartaxi.dto.common.IntegerStatusUpdateDTO;
import com.cartaxi.service.AdminUserService;
import com.cartaxi.vo.admin.user.SysUserVO;
import com.cartaxi.vo.common.PageResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@LoginRequired(roles = {"ADMIN", "SUPER_ADMIN"})
public class SysUserAdminController {

    private final AdminUserService adminUserService;

    public SysUserAdminController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ApiResponse<PageResult<SysUserVO>> page(SysUserQueryDTO queryDTO) {
        return ApiResponse.success("查询成功", adminUserService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysUserVO> detail(@PathVariable Long id) {
        return ApiResponse.success("查询成功", adminUserService.detail(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody SysUserSaveDTO saveDTO) {
        adminUserService.create(saveDTO);
        return ApiResponse.success("新增成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody SysUserSaveDTO saveDTO) {
        adminUserService.update(id, saveDTO);
        return ApiResponse.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminUserService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody IntegerStatusUpdateDTO updateDTO) {
        adminUserService.changeStatus(id, updateDTO.getStatus());
        return ApiResponse.success("状态更新成功", null);
    }
}