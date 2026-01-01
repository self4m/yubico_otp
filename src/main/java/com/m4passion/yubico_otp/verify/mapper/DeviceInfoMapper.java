package com.m4passion.yubico_otp.verify.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.m4passion.yubico_otp.common.pojo.po.DeviceInfo;

@Mapper
public interface DeviceInfoMapper extends BaseMapper<DeviceInfo> {
}
