package com.m4passion.yubico_otp.common.utils;

/**
 * CRC16校验工具类
 * 专用于Yubico OTP协议的CRC16-CCITT校验
 * 
 * 根据Yubico官方协议规范：
 * - 使用CRC16-CCITT算法（多项式0x1021的比特反转0x8408）
 * - 完整的16字节载荷（含末尾2字节CRC16）经计算后结果必须等于0xF0B8
 * - 这是一种巧妙的数学特性，用于验证数据完整性和密钥正确性
 */
public class Crc16Util {

    /**
     * 验证Yubico OTP的16字节载荷是否完整且正确
     * 
     * <p>工作原理：
     * Yubico硬件在生成OTP时，会计算前14字节数据的CRC16值，
     * 并将其存储在最后2字节。当对整个16字节数据（包含CRC16本身）
     * 再次计算CRC16时，结果应该等于固定值0xF0B8（61624）。
     * 
     * <p>这种方式的优势：
     * - 一次计算即可验证数据完整性
     * - 无需分离和比对CRC16字段
     * - 符合Yubico官方标准实现
     *
     * @param payload 16字节的OTP解密后数据
     * @return true表示校验通过（数据完整且密钥正确），false表示校验失败
     */
    public static boolean verify(byte[] payload) {
        if (payload == null) {
            return false;
        }
        
        // CRC16初始值
        int crc = 0xFFFF;
        
        // 逐字节计算CRC16
        for (byte b : payload) {
            // 与当前字节异或
            crc ^= (b & 0xFF);
            
            // 处理8个比特位
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x0001) != 0) {
                    // 多项式0x8408（0x1021的比特反转）
                    crc = (crc >> 1) ^ 0x8408;
                } else {
                    crc = crc >> 1;
                }
            }
        }
        
        // 确保结果为16位无符号整数
        crc &= 0xFFFF;
        
        // YubiKey OTP固定验证值
        return crc == 0xF0B8;
    }
}