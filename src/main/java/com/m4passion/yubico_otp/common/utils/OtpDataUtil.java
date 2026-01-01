package com.m4passion.yubico_otp.common.utils;

import lombok.extern.slf4j.Slf4j;
import com.m4passion.yubico_otp.common.exception.ValidationException;
import com.m4passion.yubico_otp.common.pojo.bo.OtpPayload;

/**
 * OTP数据处理工具类
 * 提供Yubico OTP相关的数据转换、解析和验证功能
 */
@Slf4j
public class OtpDataUtil {

    /**
     * 从完整OTP代码中提取公开标识（前12位）
     * 
     * @param otp 完整的44位OTP（44位Modhex字符）
     * @return 12位十六进制公开标识
     */
    public static String getPrefix(String otp) {
        return otp.substring(0, 12);
    }

    /**
     * 从完整OTP中提取Modhex字符（后32位）
     * 
     * @param otp 完整的44位OTP（12位公开标识 + 32位Modhex字符）
     * @return 32位Modhex编码的字符
     */
    public static String getOtpEncryptedPayload(String otp) {
        return otp.substring(otp.length() - 32);
    }

    /**
     * 验证OTP：解密并验证CRC16校验和
     * 
     * @param info      日志信息前缀，用于追踪
     * @param aeskey    32位十六进制密钥字符串（16字节）
     * @param otp       完整的44位OTP
     * @return 解密后的16字节载荷数据
     * @throws ValidationException 验证失败时抛出
     */
    public static byte[] decrypt(String info, String aeskey, String otp) {
        String otpEncryptedPayload = getOtpEncryptedPayload(otp);
        return AesCryptUtil.decrypt(info, aeskey, otpEncryptedPayload);
    }

    /**
     * 十六进制字符串转字节数组
     * 
     * @param hex 十六进制字符串（偶数长度）
     * @return 字节数组
     */
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 每两位十六进制字符转换为一个字节
            String hexByteStr = hex.substring(i, i + 2);
            int intValue = Integer.parseInt(hexByteStr, 16);
            byte byteValue = (byte) intValue;
            int arrayIndex = i / 2;
            result[arrayIndex] = byteValue;
        }
        return result;
    }

    /**
     * 字节数组转十六进制字符串
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串（小写）
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString();
    }

    /**
     * Modhex编码转换为十六进制编码
     * 
     * <p>Modhex是Yubico定义的修改版十六进制编码，
     * 使用16个特定字符（cbdefghijklnrtuv）来避免键盘布局差异问题。
     * 
     * @param modhex Modhex编码字符串
     * @return 标准十六进制字符串
     */
    public static String modhexToHex(String modhex) {
        return modhex
                .replace('c', '0')
                .replace('b', '1')
                .replace('d', '2')
                .replace('e', '3')
                .replace('f', '4')
                .replace('g', '5')
                .replace('h', '6')
                .replace('i', '7')
                .replace('j', '8')
                .replace('k', '9')
                .replace('l', 'a')
                .replace('n', 'b')
                .replace('r', 'c')
                .replace('t', 'd')
                .replace('u', 'e')
                .replace('v', 'f');
    }

    /**
     * 解析OTP载荷数据
     * 
     * <p>载荷结构（16字节）：
     * <ul>
     *   <li>字节 0-5：私有标识（6字节）</li>
     *   <li>字节 6-7：使用计数器（2字节，小端序）- 设备重新上电时递增</li>
     *   <li>字节 8-10：时间戳（3字节，小端序）- 自上电后的8ms计数</li>
     *   <li>字节 11：会话计数器（1字节）- 同一上电周期内递增</li>
     *   <li>字节 12-13：随机数（2字节，小端序）</li>
     *   <li>字节 14-15：CRC16校验和（2字节，小端序）</li>
     * </ul>
     * 
     * @param payload 16字节的OTP解密后载荷
     * @return 解析后的OtpPayload对象
     */
    public static OtpPayload getPayload(byte[] payload) {
        OtpPayload otpPayload = new OtpPayload();

        // 字节 0-5：私有标识（6字节）
        byte[] uid = new byte[6];
        System.arraycopy(payload, 0, uid, 0, 6);
        otpPayload.setUid(bytesToHex(uid));

        // 字节 6-7：使用计数器（2字节，小端序）
        int usageCounter = ((payload[7] & 0xFF) << 8) | (payload[6] & 0xFF);
        otpPayload.setUsageCounter(usageCounter);

        // 字节 8-10：时间戳（3字节，小端序，24位）
        int timestamp = ((payload[10] & 0xFF) << 16) | ((payload[9] & 0xFF) << 8) | (payload[8] & 0xFF);
        otpPayload.setTimestamp(timestamp);

        // 字节 11：会话计数器（1字节）
        int sessionUse = payload[11] & 0xFF;
        otpPayload.setSessionUse(sessionUse);

        // 字节 12-13：随机数（2字节，小端序）
        int random = ((payload[13] & 0xFF) << 8) | (payload[12] & 0xFF);
        otpPayload.setRandom(random);

        // 字节 14-15：CRC16校验和（2字节，小端序）
        int crc16 = ((payload[15] & 0xFF) << 8) | (payload[14] & 0xFF);
        otpPayload.setCrc16(crc16);

        return otpPayload;
    }
}
