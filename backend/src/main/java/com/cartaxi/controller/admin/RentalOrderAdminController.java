package com.cartaxi.controller.admin;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.dto.admin.order.RentalOrderQueryDTO;
import com.cartaxi.dto.admin.order.RentalOrderSaveDTO;
import com.cartaxi.dto.common.StringStatusUpdateDTO;
import com.cartaxi.service.AdminRentalOrderService;
import com.cartaxi.vo.admin.order.RentalOrderFormOptionsVO;
import com.cartaxi.vo.admin.order.RentalOrderVO;
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
@RequestMapping("/api/admin/orders")
@LoginRequired(roles = {"ADMIN", "SUPER_ADMIN"})
public class RentalOrderAdminController {

    private final AdminRentalOrderService adminRentalOrderService;

    public RentalOrderAdminController(AdminRentalOrderService adminRentalOrderService) {
        this.adminRentalOrderService = adminRentalOrderService;
    }

    @GetMapping
    public ApiResponse<PageResult<RentalOrderVO>> page(RentalOrderQueryDTO queryDTO) {
        return ApiResponse.success("查询成功", adminRentalOrderService.page(queryDTO));
    }

    @GetMapping("/form-options")
    public ApiResponse<RentalOrderFormOptionsVO> formOptions() {
        return ApiResponse.success("查询成功", adminRentalOrderService.formOptions());
    }

    @GetMapping("/{id}")
    public ApiResponse<RentalOrderVO> detail(@PathVariable Long id) {
        return ApiResponse.success("查询成功", adminRentalOrderService.detail(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody RentalOrderSaveDTO saveDTO) {
        adminRentalOrderService.create(saveDTO);
        return ApiResponse.success("新增成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody RentalOrderSaveDTO saveDTO) {
        adminRentalOrderService.update(id, saveDTO);
        return ApiResponse.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminRentalOrderService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody StringStatusUpdateDTO updateDTO) {
        adminRentalOrderService.changeStatus(id, updateDTO.getStatus());
        return ApiResponse.success("状态更新成功", null);
    }
}