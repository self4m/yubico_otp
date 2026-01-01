package com.m4passion.yubico_otp.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JsonResult {

    private String message;
    private String status;
    private Object data;

    /**
     * 响应自定义状态码 + 有响应数据
     */
    public JsonResult(StatusCode statusCode, Object data) {
        this.message = statusCode.getMessage();
        this.status = statusCode.getStatus();
        this.data = data;
    }

    /**
     * 响应自定义状态码
     */
    public JsonResult(StatusCode statusCode) {
        this.message = statusCode.getMessage();
        this.status = statusCode.getStatus();
    }

    /**
     * 验证成功默认消息体
     */
    public JsonResult(Object data) {
        this.data = data;
        this.status = StatusCode.SUCCESS.getStatus();
    }

    /**
     * 验证成功 有响应数据
     */
    public static JsonResult ok(Object data) {
        return new JsonResult(data);
    }

    /**
     * 验证成功 无响应数据
     */
    public static JsonResult ok() {
        return ok(null);
    }

    /**
     * 验证失败 响应自定义状态码
     */
    public static JsonResult error(StatusCode statusCode) {
        return new JsonResult(statusCode);
    }
}
