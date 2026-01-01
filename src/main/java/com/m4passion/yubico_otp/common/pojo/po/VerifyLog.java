package com.m4passion.yubico_otp.common.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * OTP验证日志记录实体
 * 对应数据库表：verify_log
 * 记录所有OTP验证请求（成功和失败）
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@TableName("verify_log")
public class VerifyLog {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    Integer id;

    /**
     * 硬件序列号
     */
    Integer serial;

    /**
     * 公开标识（12位十六进制字符）
     * 从OTP中提取的公开标识
     */
    String prefix;

    /**
     * 使用计数器
     * 记录本次OTP的使用计数器值
     */
    Integer usageCounter;

    /**
     * 会话计数器
     * 记录本次OTP的会话计数器值
     */
    Integer sessionUse;

    /**
     * 时间戳
     * 记录本次OTP的时间戳值（相对值）
     */
    Integer timestamp;

    /**
     * 验证时间
     * 记录验证请求的时间
     */
    LocalDateTime verifyTime;

    /**
     * 验证结果
     * true表示验证成功，false表示验证失败
     */
    Boolean verifyResult;

    /**
     * 失败原因
     * 验证成功时为null，失败时记录具体原因
     */
    String message;
}