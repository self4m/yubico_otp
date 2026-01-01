package com.m4passion.yubico_otp.common.exception;

import lombok.Getter;
import com.m4passion.yubico_otp.common.response.StatusCode;

public class ValidationException extends RuntimeException {
    @Getter
    private final StatusCode statusCode;

    public ValidationException(StatusCode statusCode) {
        this.statusCode = statusCode;
    }
}
