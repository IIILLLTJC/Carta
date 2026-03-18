package com.cartaxi.service.impl;

import com.cartaxi.common.exception.BusinessException;
import com.cartaxi.common.util.UploadPathResolver;
import com.cartaxi.service.UploadService;
import com.cartaxi.vo.common.UploadFileVO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadServiceImpl implements UploadService {

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_SCENES = Set.of("avatar", "car", "common");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final String uploadBaseDir;

    public UploadServiceImpl(@Value("${cartaxi.upload.base-dir:uploads}") String uploadBaseDir) {
        this.uploadBaseDir = uploadBaseDir;
    }

    @Override
    public UploadFileVO uploadImage(MultipartFile file, String scene) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException(400, "\u56fe\u7247\u5927\u5c0f\u4e0d\u80fd\u8d85\u8fc7 10MB");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException(400, "仅支持图片上传");
        }

        String normalizedScene = normalizeScene(scene);
        String extension = resolveExtension(file.getOriginalFilename(), contentType);
        String dateFolder = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String storedFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path basePath = UploadPathResolver.resolveBasePath(uploadBaseDir);
        Path targetDir = basePath.resolve(normalizedScene).resolve(dateFolder);
        Path targetFile = targetDir.resolve(storedFileName);

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetFile);
        } catch (IOException ex) {
            throw new BusinessException(500, "图片上传失败");
        }

        String url = "/uploads/" + normalizedScene + "/" + dateFolder + "/" + storedFileName;
        return UploadFileVO.builder()
                .originalName(StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : storedFileName)
                .url(url)
                .size(file.getSize())
                .build();
    }

    private String normalizeScene(String scene) {
        String normalized = StringUtils.hasText(scene) ? scene.trim().toLowerCase(Locale.ROOT) : "common";
        if (!ALLOWED_SCENES.contains(normalized)) {
            throw new BusinessException(400, "上传场景不支持");
        }
        return normalized;
    }

    private String resolveExtension(String originalFilename, String contentType) {
        String extension = null;
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        }
        if (!StringUtils.hasText(extension)) {
            extension = switch (contentType.toLowerCase(Locale.ROOT)) {
                case "image/jpeg", "image/jpg" -> "jpg";
                case "image/png" -> "png";
                case "image/gif" -> "gif";
                case "image/webp" -> "webp";
                default -> null;
            };
        }
        if (!StringUtils.hasText(extension) || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(400, "仅支持 JPG、PNG、GIF、WEBP 图片");
        }
        return extension;
    }
}
