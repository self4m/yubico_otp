package com.m4passion.yubico_otp.common.pojo.dto;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * 硬件设备注册参数
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeviceInfoParam {

    /**
     * 硬件序列号
     * 用于标识硬件设备
     */
    @NotNull(message = "硬件序列号不能为空")
    @Min(value = 1, message = "硬件序列号不能小于1")
    Integer serial;

    /**
     * 公开标识（12位Modhex字符）
     * OTP代码的前12位，公开可见，用于查找设备
     */
    @NotBlank(message = "公开标识不能为空")
    @Size(min = 12, max = 12, message = "公开标识长度必须为12位")
    @Pattern(regexp = "^[cbdefghijklnrtuv]{12}$", message = "公开标识必须为12位Modhex字符")
    String prefix;

    /**
     * 私有标识（12位十六进制字符）
     * 存储在设备和服务器端，用于验证OTP归属
     */
    @NotBlank(message = "私有标识不能为空")
    @Size(min = 12, max = 12, message = "私有标识长度必须为12位")
    @Pattern(regexp = "^[0-9a-f]{12}$", message = "私有标识必须为12位十六进制字符")
    String uid;

    /**
     * AES加密密钥（32位十六进制字符，表示16字节）
     * 用于解密OTP的密文部分
     */
    @NotBlank(message = "加密密钥不能为空")
    @Size(min = 32, max = 32, message = "加密密钥长度必须为32位")
    @Pattern(regexp = "^[0-9a-f]{32}$", message = "加密密钥必须为32位十六进制字符")
    String aeskey;

    /**
     * 一次性OTP代码（44位Modhex字符）
     * 用于注册时验证设备和密钥的正确性
     */
    @NotBlank(message = "一次性OTP代码不能为空")
    @Size(min = 44, max = 44, message = "一次性OTP代码长度必须为44位")
    @Pattern(regexp = "^[cbdefghijklnrtuv]{44}$", message = "一次性OTP代码必须为44位Modhex字符")
    String otp;
}