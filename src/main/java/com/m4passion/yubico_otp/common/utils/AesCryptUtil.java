package com.m4passion.yubico_otp.common.utils;

import lombok.extern.slf4j.Slf4j;
import com.m4passion.yubico_otp.common.exception.ValidationException;
import com.m4passion.yubico_otp.common.response.StatusCode;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密解密工具类
 * 用于Yubico OTP的AES-128-ECB解密和CRC16验证
 */
@Slf4j
public class AesCryptUtil {

    /**
     * 解密OTP并验证数据完整性
     * 
     * @param info         日志信息前缀，用于追踪
     * @param aeskey       32位十六进制密钥字符串（16字节）
     * @param otpEncryptedPayload   32位Modhex编码的字符
     * @return 解密后的16字节载荷数据
     * @throws ValidationException 当解密失败或CRC16校验失败时抛出
     */
    public static byte[] decrypt(String info, String aeskey, String otpEncryptedPayload) {
        try {
            // 初始化AES密钥和加密器
            SecretKeySpec key = new SecretKeySpec(OtpDataUtil.hexToBytes(aeskey), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            
            // 将Modhex转换为十六进制，再转换为字节数组
            byte[] otpEncryptedPayloadTextBytes = OtpDataUtil.hexToBytes(OtpDataUtil.modhexToHex(otpEncryptedPayload));
            
            // 执行AES解密
            byte[] payload = cipher.doFinal(otpEncryptedPayloadTextBytes);
            
            // 验证载荷长度必须是16字节
            if (payload == null || payload.length != 16){
                log.warn("{} 解密错误 - 载荷长度非法: {}", info, payload == null ? "null" : payload.length);
                throw new ValidationException(StatusCode.DECRYPT_DATA_LENGTH_ERROR);
            }
            
            // 验证CRC16校验和，确保密钥和OTP匹配
            if (!Crc16Util.verify(payload)) {
                log.warn("{} OTP验证失败 - CRC16校验未通过，注册信息无法正确解密OTP载荷数据", info);
                throw new ValidationException(StatusCode.SECRET_KEY_CANNOT_DECRYPT_OTP);
            }
            
            log.debug("{} OTP代码载荷信息解密成功", info);
            return payload;

        } catch (Exception e) {
            // 如果是自定义异常，则直接抛出进行返回
            if (e instanceof ValidationException){
                throw (ValidationException) e;
            }
            log.error("{} 解密操作异常", info, e);
            throw new ValidationException(StatusCode.SECRET_KEY_DECRYPT_ERROR);
        }
    }
}