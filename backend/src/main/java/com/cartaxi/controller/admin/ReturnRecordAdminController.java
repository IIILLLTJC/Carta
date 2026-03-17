package com.cartaxi.controller.admin;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.dto.admin.returnrecord.ReturnRecordQueryDTO;
import com.cartaxi.dto.admin.returnrecord.ReturnRecordSaveDTO;
import com.cartaxi.dto.common.StringStatusUpdateDTO;
import com.cartaxi.service.AdminReturnRecordService;
import com.cartaxi.vo.admin.returnrecord.ReturnRecordFormOptionsVO;
import com.cartaxi.vo.admin.returnrecord.ReturnRecordVO;
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
@RequestMapping("/api/admin/returns")
@LoginRequired(roles = {"ADMIN", "SUPER_ADMIN"})
public class ReturnRecordAdminController {

    private final AdminReturnRecordService adminReturnRecordService;

    public ReturnRecordAdminController(AdminReturnRecordService adminReturnRecordService) {
        this.adminReturnRecordService = adminReturnRecordService;
    }

    @GetMapping
    public ApiResponse<PageResult<ReturnRecordVO>> page(ReturnRecordQueryDTO queryDTO) {
        return ApiResponse.success("查询成功", adminReturnRecordService.page(queryDTO));
    }

    @GetMapping("/form-options")
    public ApiResponse<ReturnRecordFormOptionsVO> formOptions() {
        return ApiResponse.success("查询成功", adminReturnRecordService.formOptions());
    }

    @GetMapping("/{id}")
    public ApiResponse<ReturnRecordVO> detail(@PathVariable Long id) {
        return ApiResponse.success("查询成功", adminReturnRecordService.detail(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody ReturnRecordSaveDTO saveDTO) {
        adminReturnRecordService.create(saveDTO);
        return ApiResponse.success("新增成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody ReturnRecordSaveDTO saveDTO) {
        adminReturnRecordService.update(id, saveDTO);
        return ApiResponse.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminReturnRecordService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody StringStatusUpdateDTO updateDTO) {
        adminReturnRecordService.changeStatus(id, updateDTO.getStatus());
        return ApiResponse.success("状态更新成功", null);
    }
}