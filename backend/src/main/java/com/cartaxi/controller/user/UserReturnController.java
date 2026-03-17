package com.cartaxi.controller.user;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.dto.user.returnrecord.UserReturnCreateDTO;
import com.cartaxi.dto.user.returnrecord.UserReturnQueryDTO;
import com.cartaxi.service.UserReturnService;
import com.cartaxi.vo.common.PageResult;
import com.cartaxi.vo.user.returnrecord.UserReturnFormOptionsVO;
import com.cartaxi.vo.user.returnrecord.UserReturnVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/returns")
@LoginRequired(roles = {"USER"})
public class UserReturnController {

    private final UserReturnService userReturnService;

    public UserReturnController(UserReturnService userReturnService) {
        this.userReturnService = userReturnService;
    }

    @GetMapping
    public ApiResponse<PageResult<UserReturnVO>> page(UserReturnQueryDTO queryDTO) {
        return ApiResponse.success("查询成功", userReturnService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserReturnVO> detail(@PathVariable Long id) {
        return ApiResponse.success("查询成功", userReturnService.detail(id));
    }

    @GetMapping("/form-options")
    public ApiResponse<UserReturnFormOptionsVO> formOptions() {
        return ApiResponse.success("查询成功", userReturnService.formOptions());
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody UserReturnCreateDTO createDTO) {
        userReturnService.create(createDTO);
        return ApiResponse.success("归还申请提交成功", null);
    }
}