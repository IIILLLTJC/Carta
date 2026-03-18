package com.cartaxi.service;

import com.cartaxi.vo.common.UploadFileVO;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    UploadFileVO uploadImage(MultipartFile file, String scene);
}
