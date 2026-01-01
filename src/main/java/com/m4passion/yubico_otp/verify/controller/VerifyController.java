package com.m4passion.yubico_otp.verify.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import com.m4passion.yubico_otp.common.exception.ValidationException;
import com.m4passion.yubico_otp.common.pojo.dto.DeviceInfoParam;
import com.m4passion.yubico_otp.common.pojo.dto.VerifyParam;
import com.m4passion.yubico_otp.common.pojo.vo.GetSerialVO;
import com.m4passion.yubico_otp.common.pojo.vo.VerifyResultVO;
import com.m4passion.yubico_otp.common.response.JsonResult;
import com.m4passion.yubico_otp.verify.service.VerifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Yubico OTP demo 简化版控制器
 * 仿照 <a href="https://upload.yubico.com/">...</a>  实现硬件注册逻辑
 * 仿照 <a href="https://demo.yubico.com/otp/verify/">...</a>  实现OTP验证逻辑
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/otp")
public class VerifyController {

    @Autowired
    private VerifyService verifyService;

    /**
     * 注册硬件设备
     *
     * <p>接口说明：
     * <ul>
     *   <li>验证密钥格式和OTP有效性</li>
     *   <li>检查设备是否已注册（防止重复注册）</li>
     *   <li>验证私有标识匹配</li>
     *   <li>初始化防重放计数器</li>
     * </ul>
     *
     * @param deviceInfoParam 注册参数（序列号、公开标识、私有标识、密钥、OTP）
     * @return 注册成功返回成功响应
     * @throws ValidationException 验证失败时抛出
     */
    @PostMapping("upload")
    public JsonResult upload(@Valid @RequestBody DeviceInfoParam deviceInfoParam) {
        verifyService.upload(deviceInfoParam);
        return JsonResult.ok();
    }

    /**
     * 验证OTP有效性
     *
     * <p>接口说明：
     * <ul>
     *   <li>从OTP中自动提取公开标识（前12位）</li>
     *   <li>查询设备注册信息</li>
     *   <li>使用密钥解密并验证CRC16校验和</li>
     *   <li>验证私有标识匹配</li>
     *   <li>验证计数器防重放攻击</li>
     *   <li>更新计数器并记录验证日志</li>
     * </ul>
     *
     * @param verifyParam 验证参数（仅需44位OTP）
     * @return 验证成功返回成功响应
     * @throws ValidationException 验证失败时抛出
     */
    @PostMapping("verify")
    public JsonResult verify(@Valid @RequestBody VerifyParam verifyParam) {
        VerifyResultVO verifyResultVO = verifyService.verify(verifyParam);
        return JsonResult.ok(verifyResultVO);
    }

    /**
     * 获取序列号
     *
     * <p>接口说明：
     * <ul>
     *   <li>通过公开标识计算设备序列号</li>
     *   <li>只适用于默认的公开标识</li>
     *   <li>自定义的公开表示与设备序列号无关联性</li>
     * </ul>
     *
     * @param verifyParam 验证参数（仅需44位OTP）
     * @return 验证成功返回成功响应
     * @throws ValidationException 验证失败时抛出
     */
    @PostMapping("getSerial")
    public JsonResult getSerial(@Valid @RequestBody VerifyParam verifyParam) {
        GetSerialVO getSerialVO = verifyService.getSerial(verifyParam);
        return JsonResult.ok(getSerialVO);
    }
}