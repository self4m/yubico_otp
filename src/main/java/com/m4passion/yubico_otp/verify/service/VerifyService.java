package com.m4passion.yubico_otp.verify.service;

import com.m4passion.yubico_otp.common.pojo.dto.DeviceInfoParam;
import com.m4passion.yubico_otp.common.pojo.dto.VerifyParam;
import com.m4passion.yubico_otp.common.pojo.vo.GetSerialVO;
import com.m4passion.yubico_otp.common.pojo.vo.VerifyResultVO;

public interface VerifyService {
    void upload(DeviceInfoParam deviceInfoParam);

    VerifyResultVO verify(VerifyParam verifyParam);

    GetSerialVO getSerial(VerifyParam verifyParam);
}
