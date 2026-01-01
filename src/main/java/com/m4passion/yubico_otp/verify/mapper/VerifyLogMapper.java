package com.m4passion.yubico_otp.verify.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.m4passion.yubico_otp.common.pojo.po.VerifyLog;

/**
 * OTP 验证日志 Mapper
 */
@Mapper
public interface VerifyLogMapper extends BaseMapper<VerifyLog> {
}
