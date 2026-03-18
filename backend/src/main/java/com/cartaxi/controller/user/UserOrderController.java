package com.cartaxi.controller.user;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.dto.user.order.UserOrderCreateDTO;
import com.cartaxi.dto.user.order.UserOrderQueryDTO;
import com.cartaxi.service.UserOrderService;
import com.cartaxi.vo.common.PageResult;
import com.cartaxi.vo.user.order.UserOrderFormOptionsVO;
import com.cartaxi.vo.user.order.UserOrderVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/orders")
@LoginRequired(roles = {"USER"})
public class UserOrderController {

    private final UserOrderService userOrderService;

    public UserOrderController(UserOrderService userOrderService) {
        this.userOrderService = userOrderService;
    }

    @GetMapping
    public ApiResponse<PageResult<UserOrderVO>> page(UserOrderQueryDTO queryDTO) {
        return ApiResponse.success("\u67e5\u8be2\u6210\u529f", userOrderService.page(queryDTO));
    }

    @GetMapping("/form-options")
    public ApiResponse<UserOrderFormOptionsVO> formOptions() {
        return ApiResponse.success("\u67e5\u8be2\u6210\u529f", userOrderService.formOptions());
    }

    @GetMapping("/{id}")
    public ApiResponse<UserOrderVO> detail(@PathVariable Long id) {
        return ApiResponse.success("\u67e5\u8be2\u6210\u529f", userOrderService.detail(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody UserOrderCreateDTO createDTO) {
        userOrderService.create(createDTO);
        return ApiResponse.success("\u4e0b\u5355\u6210\u529f", null);
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<Void> mockPay(@PathVariable Long id) {
        userOrderService.mockPay(id);
        return ApiResponse.success("\u652f\u4ed8\u6210\u529f", null);
    }
}
