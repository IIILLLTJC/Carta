package com.cartaxi.controller.admin;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.dto.admin.car.CarInfoQueryDTO;
import com.cartaxi.dto.admin.car.CarInfoSaveDTO;
import com.cartaxi.dto.common.StringStatusUpdateDTO;
import com.cartaxi.service.AdminCarInfoService;
import com.cartaxi.vo.admin.car.CarInfoVO;
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
@RequestMapping("/api/admin/cars")
@LoginRequired(roles = {"ADMIN", "SUPER_ADMIN"})
public class CarInfoAdminController {

    private final AdminCarInfoService adminCarInfoService;

    public CarInfoAdminController(AdminCarInfoService adminCarInfoService) {
        this.adminCarInfoService = adminCarInfoService;
    }

    @GetMapping
    public ApiResponse<PageResult<CarInfoVO>> page(CarInfoQueryDTO queryDTO) {
        return ApiResponse.success("查询成功", adminCarInfoService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public ApiResponse<CarInfoVO> detail(@PathVariable Long id) {
        return ApiResponse.success("查询成功", adminCarInfoService.detail(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CarInfoSaveDTO saveDTO) {
        adminCarInfoService.create(saveDTO);
        return ApiResponse.success("新增成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody CarInfoSaveDTO saveDTO) {
        adminCarInfoService.update(id, saveDTO);
        return ApiResponse.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminCarInfoService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody StringStatusUpdateDTO updateDTO) {
        adminCarInfoService.changeStatus(id, updateDTO.getStatus());
        return ApiResponse.success("状态更新成功", null);
    }
}