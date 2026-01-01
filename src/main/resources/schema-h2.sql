-- 创建 device_info 表存储用户信息
CREATE TABLE IF NOT EXISTS device_info
(
    id                 BIGINT   NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    serial             INT      NOT NULL COMMENT '硬件序列号',
    prefix             CHAR(12) NOT NULL UNIQUE COMMENT '公开标识（12位modhex字符）',
    uid                CHAR(12) NOT NULL COMMENT '私有标识（12位十六进制字符）',
    aeskey             CHAR(32) NOT NULL COMMENT 'AES加密密钥（32位十六进制字符，表示16字节）',
    register_time      DATETIME          DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    last_usage_counter INT               DEFAULT NULL COMMENT '最后一次验证的 Usage Counter 使用计数器(防重放)',
    last_session_use   INT               DEFAULT NULL COMMENT '最后一次验证的 Session Use 会话使用次数(防重放)',
    last_timestamp     INT               DEFAULT NULL COMMENT '最后一次验证的 内部时间戳',
    last_verify_time   DATETIME          DEFAULT NULL COMMENT '最后一次验证时间',
    version            INT      NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
);

-- 创建 verify_log 表存储验证日志
CREATE TABLE IF NOT EXISTS verify_log
(
    id            BIGINT   NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    serial        INT          DEFAULT NULL COMMENT '硬件序列号',
    prefix        CHAR(12) NOT NULL COMMENT '公开标识（12位十六进制字符）',
    usage_counter INT          DEFAULT NULL COMMENT '使用计数器',
    session_use   INT          DEFAULT NULL COMMENT '会话使用次数',
    timestamp     INT          DEFAULT NULL COMMENT '内部时间戳',
    verify_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '验证时间',
    verify_result BOOLEAN  NOT NULL COMMENT '验证结果 (true: 成功, false: 失败)',
    message       VARCHAR(255) DEFAULT NULL COMMENT '失败原因'
);