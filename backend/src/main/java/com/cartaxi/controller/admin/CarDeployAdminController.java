package com.cartaxi.controller.admin;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.dto.admin.deploy.CarDeployRecordQueryDTO;
import com.cartaxi.dto.admin.deploy.CarDeployRecordSaveDTO;
import com.cartaxi.dto.common.StringStatusUpdateDTO;
import com.cartaxi.service.AdminCarDeployService;
import com.cartaxi.vo.admin.deploy.CarDeployFormOptionsVO;
import com.cartaxi.vo.admin.deploy.CarDeployRecordVO;
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
@RequestMapping("/api/admin/deploy-records")
@LoginRequired(roles = {"ADMIN", "SUPER_ADMIN"})
public class CarDeployAdminController {

    private final AdminCarDeployService adminCarDeployService;

    public CarDeployAdminController(AdminCarDeployService adminCarDeployService) {
        this.adminCarDeployService = adminCarDeployService;
    }

    @GetMapping
    public ApiResponse<PageResult<CarDeployRecordVO>> page(CarDeployRecordQueryDTO queryDTO) {
        return ApiResponse.success("查询成功", adminCarDeployService.page(queryDTO));
    }

    @GetMapping("/form-options")
    public ApiResponse<CarDeployFormOptionsVO> formOptions() {
        return ApiResponse.success("查询成功", adminCarDeployService.formOptions());
    }

    @GetMapping("/{id}")
    public ApiResponse<CarDeployRecordVO> detail(@PathVariable Long id) {
        return ApiResponse.success("查询成功", adminCarDeployService.detail(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CarDeployRecordSaveDTO saveDTO) {
        adminCarDeployService.create(saveDTO);
        return ApiResponse.success("新增成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody CarDeployRecordSaveDTO saveDTO) {
        adminCarDeployService.update(id, saveDTO);
        return ApiResponse.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminCarDeployService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody StringStatusUpdateDTO updateDTO) {
        adminCarDeployService.changeStatus(id, updateDTO.getStatus());
        return ApiResponse.success("状态更新成功", null);
    }
}