package com.cartaxi.controller.admin;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.dto.admin.recovery.CarRecoveryRecordQueryDTO;
import com.cartaxi.dto.admin.recovery.CarRecoveryRecordSaveDTO;
import com.cartaxi.dto.common.StringStatusUpdateDTO;
import com.cartaxi.service.AdminCarRecoveryService;
import com.cartaxi.vo.admin.recovery.CarRecoveryFormOptionsVO;
import com.cartaxi.vo.admin.recovery.CarRecoveryRecordVO;
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
@RequestMapping("/api/admin/recovery-records")
@LoginRequired(roles = {"ADMIN", "SUPER_ADMIN"})
public class CarRecoveryAdminController {

    private final AdminCarRecoveryService adminCarRecoveryService;

    public CarRecoveryAdminController(AdminCarRecoveryService adminCarRecoveryService) {
        this.adminCarRecoveryService = adminCarRecoveryService;
    }

    @GetMapping
    public ApiResponse<PageResult<CarRecoveryRecordVO>> page(CarRecoveryRecordQueryDTO queryDTO) {
        return ApiResponse.success("查询成功", adminCarRecoveryService.page(queryDTO));
    }

    @GetMapping("/form-options")
    public ApiResponse<CarRecoveryFormOptionsVO> formOptions() {
        return ApiResponse.success("查询成功", adminCarRecoveryService.formOptions());
    }

    @GetMapping("/{id}")
    public ApiResponse<CarRecoveryRecordVO> detail(@PathVariable Long id) {
        return ApiResponse.success("查询成功", adminCarRecoveryService.detail(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CarRecoveryRecordSaveDTO saveDTO) {
        adminCarRecoveryService.create(saveDTO);
        return ApiResponse.success("新增成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody CarRecoveryRecordSaveDTO saveDTO) {
        adminCarRecoveryService.update(id, saveDTO);
        return ApiResponse.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminCarRecoveryService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody StringStatusUpdateDTO updateDTO) {
        adminCarRecoveryService.changeStatus(id, updateDTO.getStatus());
        return ApiResponse.success("状态更新成功", null);
    }
}