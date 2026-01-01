package com.m4passion.yubico_otp.common.pojo.bo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * OTP载荷数据对象
 * 用于存储解密后的Yubico OTP 16字节载荷数据
 * 
 * <p>载荷结构说明：
 * <pre>
 * +--------+--------+----------+------------+--------+-------+
 * | 字节位 | 0-5    | 6-7      | 8-10       | 11     | 12-13 | 14-15 |
 * +--------+--------+----------+------------+--------+-------+
 * | 字段   | 私有ID | 使用计数 | 时间戳     | 会话   | 随机数 | CRC16 |
 * |        |        | 器       | (相对值)   | 计数器 |        |       |
 * +--------+--------+----------+------------+--------+-------+
 * </pre>
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OtpPayload {

    /**
     * 私有标识（6字节，十六进制字符串）
     * 用于验证OTP是否属于正确的设备
     */
    String uid;

    /**
     * 使用计数器（2字节，16位无符号整数）
     * 设备重新上电时递增，同一上电周期内保持不变
     * 用于防重放攻击的主计数器
     */
    Integer usageCounter;

    /**
     * 时间戳（3字节，24位无符号整数）
     * 记录自设备上电后的相对时间（单位：约8毫秒）
     * 用于辅助验证OTP的新鲜度
     */
    Integer timestamp;

    /**
     * 会话计数器（1字节，8位无符号整数）
     * 在同一上电周期内，每次按下按钮递增
     * 设备重新上电后归零
     * 用于防重放攻击的副计数器
     */
    Integer sessionUse;

    /**
     * 随机数（2字节，16位无符号整数）
     * 由设备硬件生成的随机值
     * 增加密码学强度，防止预测
     */
    Integer random;

    /**
     * CRC16校验和（2字节，16位无符号整数）
     * 前14字节数据的CRC16-CCITT校验和
     * 用于验证数据完整性和密钥正确性
     */
    Integer crc16;
}
