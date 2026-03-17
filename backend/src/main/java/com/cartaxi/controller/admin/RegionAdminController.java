package com.cartaxi.controller.admin;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.dto.admin.region.RegionQueryDTO;
import com.cartaxi.dto.admin.region.RegionSaveDTO;
import com.cartaxi.dto.common.IntegerStatusUpdateDTO;
import com.cartaxi.entity.Region;
import com.cartaxi.service.AdminRegionService;
import com.cartaxi.vo.admin.region.RegionOptionVO;
import com.cartaxi.vo.common.PageResult;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/regions")
@LoginRequired(roles = {"ADMIN", "SUPER_ADMIN"})
public class RegionAdminController {

    private final AdminRegionService adminRegionService;

    public RegionAdminController(AdminRegionService adminRegionService) {
        this.adminRegionService = adminRegionService;
    }

    @GetMapping
    public ApiResponse<PageResult<Region>> page(RegionQueryDTO queryDTO) {
        return ApiResponse.success("查询成功", adminRegionService.page(queryDTO));
    }

    @GetMapping("/options")
    public ApiResponse<List<RegionOptionVO>> options() {
        return ApiResponse.success("查询成功", adminRegionService.options());
    }

    @GetMapping("/{id}")
    public ApiResponse<Region> detail(@PathVariable Long id) {
        return ApiResponse.success("查询成功", adminRegionService.detail(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody RegionSaveDTO saveDTO) {
        adminRegionService.create(saveDTO);
        return ApiResponse.success("新增成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody RegionSaveDTO saveDTO) {
        adminRegionService.update(id, saveDTO);
        return ApiResponse.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminRegionService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody IntegerStatusUpdateDTO updateDTO) {
        adminRegionService.changeStatus(id, updateDTO.getStatus());
        return ApiResponse.success("状态更新成功", null);
    }
}