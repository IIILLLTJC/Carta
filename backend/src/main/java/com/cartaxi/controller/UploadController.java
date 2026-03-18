package com.cartaxi.controller;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.service.UploadService;
import com.cartaxi.vo.common.UploadFileVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/common/upload")
@LoginRequired
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/image")
    public ApiResponse<UploadFileVO> uploadImage(@RequestParam("file") MultipartFile file,
                                                 @RequestParam(value = "scene", required = false) String scene) {
        return ApiResponse.success("上传成功", uploadService.uploadImage(file, scene));
    }
}
