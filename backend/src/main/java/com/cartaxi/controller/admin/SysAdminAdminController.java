package com.cartaxi.controller.admin;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.dto.admin.admin.SysAdminQueryDTO;
import com.cartaxi.dto.admin.admin.SysAdminSaveDTO;
import com.cartaxi.dto.common.IntegerStatusUpdateDTO;
import com.cartaxi.service.AdminSysAdminService;
import com.cartaxi.vo.admin.admin.SysAdminVO;
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
@RequestMapping("/api/admin/admins")
@LoginRequired(roles = {"ADMIN", "SUPER_ADMIN"})
public class SysAdminAdminController {

    private final AdminSysAdminService adminSysAdminService;

    public SysAdminAdminController(AdminSysAdminService adminSysAdminService) {
        this.adminSysAdminService = adminSysAdminService;
    }

    @GetMapping
    public ApiResponse<PageResult<SysAdminVO>> page(SysAdminQueryDTO queryDTO) {
        return ApiResponse.success("查询成功", adminSysAdminService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysAdminVO> detail(@PathVariable Long id) {
        return ApiResponse.success("查询成功", adminSysAdminService.detail(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody SysAdminSaveDTO saveDTO) {
        adminSysAdminService.create(saveDTO);
        return ApiResponse.success("新增成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody SysAdminSaveDTO saveDTO) {
        adminSysAdminService.update(id, saveDTO);
        return ApiResponse.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminSysAdminService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody IntegerStatusUpdateDTO updateDTO) {
        adminSysAdminService.changeStatus(id, updateDTO.getStatus());
        return ApiResponse.success("状态更新成功", null);
    }
}