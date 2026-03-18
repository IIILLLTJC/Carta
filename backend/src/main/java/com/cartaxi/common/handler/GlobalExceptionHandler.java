package com.cartaxi.common.handler;

import com.cartaxi.common.api.ApiResponse;
import com.cartaxi.common.exception.BusinessException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException exception) {
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldError() != null
                ? exception.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException exception) {
        String message = exception.getBindingResult().getFieldError() != null
                ? exception.getBindingResult().getFieldError().getDefaultMessage()
                : "参数绑定失败";
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResponse<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException exception) {
        return ApiResponse.fail(400, "\u4e0a\u4f20\u5931\u8d25\uff1a\u6587\u4ef6\u8fc7\u5927\uff0c\u6700\u5927\u652f\u6301 10MB");
    }

    @ExceptionHandler(MultipartException.class)
    public ApiResponse<Void> handleMultipartException(MultipartException exception) {
        String message = exception.getMessage();
        if (message != null && message.toLowerCase().contains("maximum upload size exceeded")) {
            return ApiResponse.fail(400, "\u4e0a\u4f20\u5931\u8d25\uff1a\u6587\u4ef6\u8fc7\u5927\uff0c\u6700\u5927\u652f\u6301 10MB");
        }
        return ApiResponse.fail(400, message != null ? message : "\u4e0a\u4f20\u8bf7\u6c42\u89e3\u6790\u5931\u8d25");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception exception) {
        return ApiResponse.fail(500, exception.getMessage());
    }
}
