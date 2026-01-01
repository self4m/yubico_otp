package com.m4passion.yubico_otp.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum StatusCode {

    // -------------------- 通用状态 --------------------
    SUCCESS("success", "success"),
    ERROR("error", "error"),
    OPERATION_FAILED("error", "操作失败"),
    VALIDATE_ERROR("error", "方法参数校验异常"),

    // -------------------- OTP解密和校验相关错误 --------------------
    SECRET_KEY_CANNOT_DECRYPT_OTP("error", "OTP验证失败，注册信息无法正确解密OTP载荷数据"),
    SECRET_KEY_DECRYPT_ERROR("error", "解密操作异常"),
    DECRYPT_DATA_LENGTH_ERROR("error", "解密失败，载荷长度非法"),
    PRIVATE_ID_MISMATCH("error", "私有标识不匹配"),
    
    // -------------------- 注册相关错误 --------------------
    PUBLIC_IDENTIFY_MISMATCH("error", "公开标识与所提交的信息不匹配"),
    PUBLIC_IDENTIFY_EXISTS("error", "公开标识已存在"),
    
    // -------------------- 验证相关错误 --------------------
    DEVICE_NOT_REGISTERED("error", "设备未注册"),
    OTP_COUNTER_TOO_LOW("error", "重放攻击"),
    OTP_ALREADY_USED("error", "重放攻击"),
    DEVICE_VERIFY_CONFLICT("error", "重放攻击"),
    OTP_SERIAL_PARSE_ERROR("error", "OTP序列号解析失败");

    private String status;
    private String message;
}
