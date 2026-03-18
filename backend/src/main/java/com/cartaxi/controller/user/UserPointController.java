package com.cartaxi.controller.user;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.dto.user.point.UserPointQueryDTO;
import com.cartaxi.service.UserPointService;
import com.cartaxi.vo.common.PageResult;
import com.cartaxi.vo.user.point.UserPointMapVO;
import com.cartaxi.vo.user.point.UserPointVO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/points")
@LoginRequired(roles = {"USER"})
public class UserPointController {

    private final UserPointService userPointService;

    public UserPointController(UserPointService userPointService) {
        this.userPointService = userPointService;
    }

    @GetMapping
    public ApiResponse<PageResult<UserPointVO>> page(UserPointQueryDTO queryDTO) {
        return ApiResponse.success("\u67e5\u8be2\u6210\u529f", userPointService.page(queryDTO));
    }

    @GetMapping("/map")
    public ApiResponse<List<UserPointMapVO>> mapPoints(@RequestParam(value = "regionName", required = false) String regionName) {
        return ApiResponse.success("\u67e5\u8be2\u6210\u529f", userPointService.mapPoints(regionName));
    }
}
