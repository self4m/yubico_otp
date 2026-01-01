package com.m4passion.yubico_otp.common.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * 硬件设备注册记录实体
 * 对应数据库表：device_info
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@TableName("device_info")
public class DeviceInfo {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    Integer id;

    /**
     * 硬件序列号
     * 用于标识硬件设备
     */
    Integer serial;

    /**
     * 公开标识（12位十六进制字符）
     * OTP的前12位，公开可见，用于查找设备
     */
    String prefix;

    /**
     * 私有标识（12位十六进制字符）
     * 存储在设备和服务器端，用于验证OTP归属
     */
    String uid;

    /**
     * AES加密密钥（32位十六进制字符，表示16字节）
     * 用于解密OTP的密文部分
     */
    String aeskey;

    /**
     * 硬件注册时间
     */
    LocalDateTime registerTime;

    /**
     * 最后一次验证的使用计数器
     * 用于防重放攻击，设备重新上电时递增
     */
    Integer lastUsageCounter;

    /**
     * 最后一次验证的会话计数器
     * 用于防重放攻击，同一上电周期内递增
     */
    Integer lastSessionUse;

    /**
     * 最后一次验证的时间戳
     */
    Integer lastTimestamp;

    /**
     * 最后一次验证时间
     */
    LocalDateTime lastVerifyTime;

    /**
     * 乐观锁版本号
     */
    Integer version;
}