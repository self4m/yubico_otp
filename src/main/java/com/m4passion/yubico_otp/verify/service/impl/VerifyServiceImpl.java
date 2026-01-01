package com.m4passion.yubico_otp.verify.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import com.m4passion.yubico_otp.common.exception.ValidationException;
import com.m4passion.yubico_otp.common.pojo.bo.OtpPayload;
import com.m4passion.yubico_otp.common.pojo.dto.DeviceInfoParam;
import com.m4passion.yubico_otp.common.pojo.dto.VerifyParam;
import com.m4passion.yubico_otp.common.pojo.po.DeviceInfo;
import com.m4passion.yubico_otp.common.pojo.po.VerifyLog;
import com.m4passion.yubico_otp.common.pojo.vo.GetSerialVO;
import com.m4passion.yubico_otp.common.pojo.vo.VerifyResultVO;
import com.m4passion.yubico_otp.common.response.StatusCode;
import com.m4passion.yubico_otp.common.utils.OtpDataUtil;
import com.m4passion.yubico_otp.verify.mapper.DeviceInfoMapper;
import com.m4passion.yubico_otp.verify.mapper.VerifyLogMapper;
import com.m4passion.yubico_otp.verify.service.VerifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class VerifyServiceImpl implements VerifyService {

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    @Autowired
    private VerifyLogMapper verifyLogMapper;

    @Value("${verify.check-serial}")
    private boolean verifyCheckSerial;

    /**
     * 硬件注册功能的实现
     * @param deviceInfoParam 注册参数（序列号、公开标识、私有标识、密钥、OTP）
     */
    @Override
    public void upload(DeviceInfoParam deviceInfoParam) {
        // 生成追踪标识，用于日志关联
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        Integer serial = deviceInfoParam.getSerial();
        String prefix = deviceInfoParam.getPrefix().trim();

        log.info("硬件注册-[{}] 开始注册 - 序列号: {}, 公开标识: {}",
                traceId, serial, prefix);

        // 1. 验证公开标识与OTP匹配：公开标识必须等于OTP的前12位
        log.debug("硬件注册-[{}] 验证公开标识是否与OTP代码匹配", traceId);
        String otp = deviceInfoParam.getOtp().trim();
        String otpPrefix = OtpDataUtil.getPrefix(otp);
        if (!prefix.equals(otpPrefix)) {
            log.warn("硬件注册-[{}] 注册失败 - 公开标识与OTP不匹配, 期望值: {}, 实际值: {}",
                    traceId, prefix, otp);
            throw new ValidationException(StatusCode.PUBLIC_IDENTIFY_MISMATCH);
        }

        // 若 verify.check-serial 的值为true，将会校验公开标识是否为设备默认值
        // 因为公开标识是序列号经过16进制转换在进行 Modhex 替换的的12位字符
        if (verifyCheckSerial){
            log.debug("硬件注册-[{}] 验证公开标识是否与序列号匹配", traceId);
            Integer otpSerial = Integer.parseInt(OtpDataUtil.modhexToHex("cc" + prefix.substring(2)), 16);
            if (!serial.equals(otpSerial)) {
                log.warn("硬件注册-[{}] 注册失败 - 公开标识与序列号不匹配, 期望值: {}, 实际值: {}",
                        traceId, serial, otpSerial);
                throw new ValidationException(StatusCode.PUBLIC_IDENTIFY_MISMATCH);
            }
        }

        // 2. 检查公开标识是否已经注册：同一序列号下不允许重复注册
        log.debug("硬件注册-[{}] 检查公开标识是否已被注册", traceId);
        QueryWrapper<DeviceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("prefix", prefix);
        List<DeviceInfo> deviceInfoList = deviceInfoMapper.selectList(queryWrapper);
        for (DeviceInfo deviceInfoItem : deviceInfoList){
            if (deviceInfoItem.getPrefix().equals(prefix)){
                log.warn("硬件注册-[{}] 注册失败 - 公开标识已存在", traceId);
                throw new ValidationException(StatusCode.PUBLIC_IDENTIFY_EXISTS);
            }
        }

        // 3. 使用密钥解密OTP并验证CRC16校验和
        log.debug("硬件注册-[{}] 解密OTP代码并验证CRC16校验和", traceId);
        String aeskey = deviceInfoParam.getAeskey().trim();
        byte[] payload = OtpDataUtil.decrypt(
                String.format("硬件注册-[%s]", traceId),
                aeskey,
                otp
        );

        // 4. 解析OTP载荷数据：提取私有标识、计数器等信息
        log.debug("硬件注册-[{}] 解析OTP代码载荷数据", traceId);
        OtpPayload otpPayload = OtpDataUtil.getPayload(payload);

        // 5. 验证私有标识匹配：防止使用他人的OTP
        String uid = deviceInfoParam.getUid().trim();
        String otpUid = otpPayload.getUid();
        if (!uid.equals(otpUid)) {
            log.warn("硬件注册-[{}] 注册失败 - 私有标识不匹配, 期望值: {}, 实际值: {}",
                    traceId, uid, otpUid);
            throw new ValidationException(StatusCode.PRIVATE_ID_MISMATCH);
        }

        // 6. 保存注册信息并初始化防重放计数器
        log.debug("硬件注册-[{}] 保存注册信息到数据库", traceId);
        LocalDateTime now = LocalDateTime.now();
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setSerial(serial);
        deviceInfo.setPrefix(prefix);
        deviceInfo.setUid(uid);
        deviceInfo.setAeskey(aeskey);
        deviceInfo.setRegisterTime(now);

        // 安全关键：初始化计数器，防止注册时使用的OTP被用于重放攻击
        // 注册即视为第一次验证，确保该OTP立即失效
        deviceInfo.setLastUsageCounter(otpPayload.getUsageCounter());
        deviceInfo.setLastSessionUse(otpPayload.getSessionUse());
        deviceInfo.setLastTimestamp(otpPayload.getTimestamp());
        deviceInfo.setLastVerifyTime(now);
        deviceInfo.setVersion(0);

        deviceInfoMapper.insert(deviceInfo);

        log.info("硬件注册-[{}] 注册成功 - 序列号: {}, 公开标识: {}, 初始计数器: {}",
                traceId, serial, prefix, otpPayload.getUsageCounter());
    }

    /**
     * OTP验证功能的实现
     * @param verifyParam 验证参数（OTP）
     */
    @Override
    public VerifyResultVO verify(VerifyParam verifyParam) {
        // 生成追踪标识，用于日志关联
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        // 从OTP中提取公开标识（前12位）
        String otp = verifyParam.getOtp().trim();
        String prefix = OtpDataUtil.getPrefix(otp);

        log.info("OTP验证-[{}] 开始验证 - 公开标识: {}", traceId, prefix);

        // 初始化验证日志记录
        LocalDateTime now = LocalDateTime.now();
        VerifyLog verifyLog = new VerifyLog();
        verifyLog.setPrefix(prefix);
        verifyLog.setVerifyTime(now);

        try {
            // 1. 根据公开标识查询注册信息
            log.debug("OTP验证-[{}] 查询设备注册信息", traceId);
            QueryWrapper<DeviceInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("prefix", prefix);
            DeviceInfo deviceInfo = deviceInfoMapper.selectOne(queryWrapper);

            if (deviceInfo == null) {
                log.warn("OTP验证-[{}] 验证失败 - 设备未注册", traceId);
                verifyLog.setVerifyResult(false);
                verifyLog.setMessage("设备未注册");
                throw new ValidationException(StatusCode.DEVICE_NOT_REGISTERED);
            }

            // 设置序列号用于日志记录
            verifyLog.setSerial(deviceInfo.getSerial());

            // 2. 使用密钥解密OTP并验证CRC16校验和
            log.debug("OTP验证-[{}] 解密OTP代码并验证CRC16校验和", traceId);
            String aeskey = deviceInfo.getAeskey();
            byte[] payload = OtpDataUtil.decrypt(
                    String.format("OTP验证-[%s]", traceId),
                    aeskey,
                    otp
            );

            // 3. 解析OTP载荷数据
            log.debug("OTP验证-[{}] 解析OTP代码载荷数据", traceId);
            OtpPayload otpPayload = OtpDataUtil.getPayload(payload);

            // 4. 验证私有标识匹配：防止使用他人的OTP
            String uid = deviceInfo.getUid();
            String otpUid = otpPayload.getUid();
            if (!uid.equals(otpUid)) {
                log.warn("OTP验证-[{}] 验证失败 - 私有标识不匹配, 期望值: {}, 实际值: {}",
                        traceId, uid, otpUid);
                verifyLog.setVerifyResult(false);
                verifyLog.setMessage("私有标识不匹配");
                throw new ValidationException(StatusCode.PRIVATE_ID_MISMATCH);
            }

            // 5. 验证使用计数器和会话计数器（防重放攻击）
            Integer currentUsageCounter = otpPayload.getUsageCounter();
            Integer lastUsageCounter = deviceInfo.getLastUsageCounter();
            log.debug("OTP验证-[{}] 使用计数器验证 - 使用计数器: 当前值: {}, 上次值: {}",
                    traceId, currentUsageCounter, lastUsageCounter);

            // 使用计数器必须 >= 上次值（重新上电使用会递增，同一次上电保持不变）
            if (lastUsageCounter != null && currentUsageCounter < lastUsageCounter) {
                log.warn("OTP验证-[{}] 使用计数器验证失败 - 使用计数器回退，重放攻击, 当前值: {}, 上次记录值: {}",
                        traceId, currentUsageCounter, lastUsageCounter);
                verifyLog.setUsageCounter(currentUsageCounter);
                verifyLog.setVerifyResult(false);
                verifyLog.setMessage("使用计数器回退，重放攻击");
                throw new ValidationException(StatusCode.OTP_COUNTER_TOO_LOW);
            }

            // 如果使用计数器相同（同一次上电使用周期），必须验证会话计数器和内部时间戳
            Integer currentSessionUse = otpPayload.getSessionUse();
            Integer lastSessionUse = deviceInfo.getLastSessionUse();
            Integer currentTimestamp = otpPayload.getTimestamp();
            Integer lastTimestamp = deviceInfo.getLastTimestamp();
            if (currentUsageCounter.equals(lastUsageCounter)) {
                log.debug("OTP验证-[{}] 使用计数器相同，将进行会话计数器和内部时间戳验证 - 会话计数器: 当前值: {}, 上次值: {}, 时间戳: 当前值: {}, 上次值: {}",
                        traceId, currentSessionUse, lastSessionUse, currentTimestamp, lastTimestamp);
                boolean sessionUseCheck = lastSessionUse != null && currentSessionUse > lastSessionUse;
                boolean timestampCheck = lastTimestamp !=null && currentTimestamp > lastTimestamp;
                // 会话计数器和内部时间戳必须严格递增
                if (!sessionUseCheck || !timestampCheck) {
                    log.warn("OTP验证-[{}] 使用计数器相同，会话计数器和内部时间戳验证失败 - 会话计数器: 当前值: {}, 上次值: {}, 时间戳: 当前值: {}, 上次值: {}",
                            traceId, currentSessionUse, lastSessionUse, currentTimestamp, lastTimestamp);
                    verifyLog.setUsageCounter(currentUsageCounter);
                    verifyLog.setSessionUse(currentSessionUse);
                    verifyLog.setTimestamp(currentTimestamp);
                    verifyLog.setVerifyResult(false);
                    verifyLog.setMessage("会话计数器或内部时间戳回退，重放攻击");
                    throw new ValidationException(StatusCode.OTP_ALREADY_USED);
                }
            } else {
                log.debug("OTP验证-[{}] 使用计数器相同，会话计数器和内部时间戳验证成功 - 会话计数器: 当前值: {}, 上次值: {}, 时间戳: 当前值: {}, 上次值: {}",
                        traceId, lastUsageCounter, currentUsageCounter, lastTimestamp, currentTimestamp);
            }

            log.info("OTP验证-[{}] 会话周期验证通过，未检测到重放攻击", traceId);

            // 6. 更新最后使用的计数器值
            Integer version = deviceInfo.getVersion();
            deviceInfo.setLastUsageCounter(currentUsageCounter);
            deviceInfo.setLastSessionUse(currentSessionUse);
            deviceInfo.setLastTimestamp(currentTimestamp);
            deviceInfo.setLastVerifyTime(now);
            deviceInfo.setVersion(version + 1);

            UpdateWrapper<DeviceInfo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", deviceInfo.getId())
                    .eq("version", version);

            log.debug("OTP验证-[{}] 更新OTP代码验证信息到数据库", traceId);

            int updateCount = deviceInfoMapper.update(deviceInfo, updateWrapper);
            if (updateCount == 0) {
                log.warn("OTP验证-[{}] 更新OTP代码验证信息到数据库失败 - 可能存在并发修改", traceId);
                throw new ValidationException(StatusCode.DEVICE_VERIFY_CONFLICT);
            }

            // 7. 记录验证成功日志
            verifyLog.setUsageCounter(currentUsageCounter);
            verifyLog.setSessionUse(currentSessionUse);
            verifyLog.setTimestamp(currentTimestamp);
            verifyLog.setVerifyResult(true);

            // 初始化返回结果
            VerifyResultVO verifyResultVO = new VerifyResultVO();
            verifyResultVO.setOtp(otp);
            verifyResultVO.setStatus("OK");
            verifyResultVO.setT(now);

            log.info("OTP验证-[{}] 验证成功 - 使用计数器: {}, 会话计数器: {}, 时间戳: {}",
                    traceId, currentUsageCounter, currentSessionUse, currentTimestamp);

            return verifyResultVO;

        } catch (ValidationException e) {
            verifyLog.setVerifyResult(false);
            verifyLog.setMessage(e.getStatusCode().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("OTP验证-[{}] 系统异常", traceId, e);
            verifyLog.setVerifyResult(false);
            verifyLog.setMessage("系统异常: " + e.getMessage());
            throw new ValidationException(StatusCode.ERROR);
        } finally {
            verifyLogMapper.insert(verifyLog);
        }
    }

    /**
     * 获取设备序列号功能的实现
     * @param verifyParam 完整的OTP代码
     * @return 设备序列号
     */
    @Override
    public GetSerialVO getSerial(VerifyParam verifyParam) {
        String otp = verifyParam.getOtp().trim();
        String prefix = otp.substring(0, 12);
        String ccModhex = "cc" + prefix.substring(2);
        String ccHex = OtpDataUtil.modhexToHex(ccModhex);
        Integer serial = null;
        try {
            serial = Integer.parseInt(ccHex, 16);
        } catch (NumberFormatException e) {
            log.warn("获取设备序列号失败 - 公开标识: {}", prefix);
            throw new ValidationException(StatusCode.OTP_SERIAL_PARSE_ERROR);
        }
        GetSerialVO getSerialVO = new GetSerialVO();
        getSerialVO.setSerial(serial);
        if (ccHex.startsWith("0")){
            int hexLength = 0;
            while (hexLength < ccHex.length() && ccHex.charAt(hexLength) == '0') {
                hexLength++;
            }
            ccHex = ccHex.substring(hexLength);
        }
        getSerialVO.setHex(ccHex.toUpperCase());
        return getSerialVO;
    }
}
