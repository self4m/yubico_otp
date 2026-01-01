package com.m4passion.yubico_otp.common.pojo.vo;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * 验证结果载荷
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifyResultVO {

    /**
     * 验证失败原因
     */
    String message;

    /**
     * 验证结果
     */
    String status;

    /**
     * 验证成功返回进行验证的OTP代码
     */
    String otp;

    /**
     * 验证时间
     */
    LocalDateTime t;

}
