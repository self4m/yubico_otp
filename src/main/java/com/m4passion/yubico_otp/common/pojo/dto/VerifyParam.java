package com.m4passion.yubico_otp.common.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * OTP验证参数
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifyParam {

    /**
     * 一次性OTP代码（44位Modhex字符）
     */
    @NotBlank(message = "一次性OTP代码不能为空")
    @Size(min = 44, max = 44, message = "一次性OTP代码长度必须为44位")
    @Pattern(regexp = "^[cbdefghijklnrtuv]{44}$", message = "一次性OTP代码必须为44位Modhex字符")
    String otp;
}