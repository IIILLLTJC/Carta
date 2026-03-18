package com.cartaxi.config;

import com.cartaxi.common.interceptor.JwtAuthInterceptor;
import com.cartaxi.common.util.UploadPathResolver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final String uploadBaseDir;

    public WebMvcConfig(JwtAuthInterceptor jwtAuthInterceptor,
                        @Value("${cartaxi.upload.base-dir:uploads}") String uploadBaseDir) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
        this.uploadBaseDir = uploadBaseDir;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/user/login",
                        "/api/auth/admin/login",
                        "/error"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = UploadPathResolver.resolveBasePath(uploadBaseDir);
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException ex) {
            throw new IllegalStateException("无法初始化上传目录", ex);
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(UploadPathResolver.toResourceLocation(uploadPath));
    }
}
