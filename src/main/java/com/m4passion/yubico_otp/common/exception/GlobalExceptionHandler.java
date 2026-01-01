package com.m4passion.yubico_otp.common.exception;

import lombok.extern.slf4j.Slf4j;
import com.m4passion.yubico_otp.common.response.JsonResult;
import com.m4passion.yubico_otp.common.response.StatusCode;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public JsonResult validationException(ValidationException ex) {
        return JsonResult.error(ex.getStatusCode());
    }

    @ExceptionHandler
    public JsonResult doHandleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        log.error("RuntimeException: {}", message);
        return new JsonResult(StatusCode.OPERATION_FAILED);
    }

    @ExceptionHandler
    public JsonResult doHandleIllegalArgumentException(IllegalArgumentException ex) {
        String message = ex.getMessage();
        log.error("IllegalArgumentException: {}", message);
        return new JsonResult(StatusCode.OPERATION_FAILED);
    }

    @ExceptionHandler
    public JsonResult doHandleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getFieldError().getDefaultMessage();
        log.warn("方法参数校验异常: {}", message);
        return new JsonResult(StatusCode.VALIDATE_ERROR, message);
    }

    @ExceptionHandler
    public JsonResult globalException(Exception ex) {
        log.error("捕获全局异常: {}", ex.getMessage(),ex);
        return new JsonResult(StatusCode.OPERATION_FAILED);
    }

}