package com.m4passion.yubico_otp.common.pojo.vo;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * 获取序列号结果载荷
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetSerialVO {

    /**
     * 序列号
     */
    Integer serial;

    /**
     * 16进制序列号
     */
    String hex;
}
