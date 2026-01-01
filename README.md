# Yubico OTP 验证系统

此项目基于Spring Boot仿照<a href="https://demo.yubico.com/otp/verify/">Yubico OTP（一次性密码）验证系统 demo</a>开发实现  
旨在实现个人支持的 YubiKey 硬件令牌的注册与一次性密码（OTP）验证功能

此项目是可以作为一个理解YubiKey OTP工作原理和验证流程的示例，仅供学习参考。  

**不建议在生产环境中使用**，原因如下：
1. **AES 密钥明文存储**：用于解密 OTP 的 AES 密钥直接以明文形式保存在数据库中，缺乏密钥保护机制，存在严重安全风险。
2. **注册过程无加密传输**：设备注册阶段的数据（如 Public ID 和密钥）通过未加密通道传输，容易遭受中间人攻击（MITM）。
3. **基于测试性质的官方 Demo**：本项目参考的 Yubico 官方演示站点本身也明确声明仅用于开发与测试，不具备生产级安全性。
4. **缺少关键安全机制**：未实现响应签名验证，且未按 Yubico 官方协议要求在请求中包含随机数（nonce），无法有效防范重放攻击。

## 项目简介

本项目实现了简单的Yubico OTP验证功能，包括：
- 硬件设备注册
- OTP代码有效性验证
- 验证日志记录

项目采用前后端一体化部署，前端页面可以简单的进行设备注册和验证

## 技术栈

- **后端框架**：Spring Boot 3.5.8
- **编程语言**：Java 17+
- **数据库**：MySQL 5.6.5+ 或 H2 
- **持久层**：MyBatis Plus 3.5.14
- **其他**：Lombok、Spring Validation

## 功能特性

### 1. 硬件设备注册
- 验证注册数据的格式
- 验证公开标识与OTP匹配
- 验证公开标识与设备序列号匹配（默认不开启）
- 检查设备是否已注册（防止重复注册）
- 验证私有标识匹配
- 初始化防重放计数器

### 2. OTP验证
- 从OTP中自动提取公开标识
- 查询设备注册信息
- 使用密钥解密并验证CRC16校验和
- 验证私有标识匹配
- 验证计数器和内部时间戳防重放攻击
- 更新计数器并记录验证日志

### 3. 安全机制
- AES-128-ECB解密
- CRC16-CCITT校验和验证
- 使用计数器和会话计数器防重放攻击
- 乐观锁机制保证数据一致性

## 项目结构

```
src/main/
├── java/com/m4passion/yubico_otp/
│   ├── common/           # 通用组件
│   │   ├── exception/    # 异常处理
│   │   ├── pojo/         # 数据对象
│   │   ├── response/     # 响应封装
│   │   └── utils/        # 工具类
│   ├── verify/           # 验证业务模块
│   │   ├── controller/   # 控制器
│   │   ├── service/      # 业务逻辑
│   │   └── mapper/       # 数据访问层
│   └── YubicoOtpApplication.java  # 启动类
└── resources/
    ├── static/           # 静态资源（前端页面）
    ├── application.properties    # 默认配置文件 （使用H2数据库）
    ├── application-mysql.properties    # MySQL配置文件
    ├── Drop-h2-table.sql         # 删除h2数据库表SQL文件（这将会删除h2数据库库中的表）
    ├── Drop-mysql-database.sql   # 删除mysql数据库表SQL文件（这将会删除mysql数据库）
    └── Init-mysql-database.sql   # 手动初始化mysql数据库SQL文件（如果存在数据库则将会删除重新创建）
```

---

## 安装

### 1. 准备运行环境

- 确保拥有Java 17 JRE环境
- 若使用MySQL数据库，请确认数据库已安装并创建好数据库和表

### 2. MySQL数据库准备（使用 H2 数据库可跳过）

> 不推荐在生产环境使用默认的 H2 数据库，这可能因为操作不当导致数据文件损坏。  
> 如果因为某些原因（如内存不足以运行独立数据库）必须要使用，建议按时备份数据。

#### 创建MySQL用户并初始化数据库

> 该 SQL 文件会创建一个名为 `yubico_otp` 的数据库   
> 同时创建一个用户名和密码均为 `yubico_otp` 的用户   
> 并授予该用户对 `yubico_otp` 数据库基础的增删改查的权限  
> 如有特殊需求，请自行修改该 SQL 文件

``` shell
# 下载 sql 命令文件
wget https://raw.githubusercontent.com/m4passion/yubico_otp/refs/heads/SpringBoot3/Init-mysql-database.sql
```

``` shell
# 使用 sql 文件初始化MySQL数据库
mysql -u root -p < ./Init-mysql-database.sql
# 输入 root 用户密码后将会执行sql文件
```

### 3. 创建系统用户

> 不推荐使用 root 权限运行，使用 root 权限可以跳过此步骤

创建一个新系统用户（名字可以随意）
``` shell
useradd -m yubico_otp
```
为 yubico_otp 用户创建密码
``` shell
passwd yubico_otp
```
登录到 yubico_otp 账户
```shell
su - yubico_otp
```
创建存放 jar包和配置文件 的目录，以 ~/yubico_otp 为例
```shell
mkdir ~/yubico_otp && cd ~/yubico_otp
```
下载运行包
```shell
wget https://github.com/m4passion/yubico_otp/releases/download/0.1.0/yubico_otp-0.1.0.jar -O yubico_otp.jar
```
创建配置文件
```shell
vim application.properties
```
将以下内容复制到 `application.properties` 中，根据下面的配置说明进行配置。
```properties
# 项目名称
spring.application.name=yubico_otp
# 运行端口
server.port=8080
# 日志级别
logging.level.com.m4passion= INFO
# 日志文件配置
logging.file.name=./yubico_otp.log

# 根据mysql部署信息修改数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/yubico_otp?characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=yubico_otp
spring.datasource.password=yubico_otp

# 公开标识与序列号的关联性的校验，若开启则只允许使用默认公开标识进行注册和使用
verify.check-serial= false

spring.jackson.default-property-inclusion= non_null
```

### 5. 启动应用

```bash
# 使用Maven启动
java -Dfile.encoding=UTF-8 -jar $HOME/yubico_otp/yubico_otp.jar --spring.config.import=optional:file:$HOME/yubico_otp/
```

### 4. 访问应用

启动成功后，访问 http://localhost:8080 查看前端页面。

## API接口

### 设备注册
- 请求URL：`/api/v1/otp/upload`
- 请求方法：POST
- 请求头：`Content-Type: application/json`
- 示例请求体：
    ```json
    {
    "serial": 123456,
    "prefix": "cccccccbdtbt",
    "uid": "000000000000",
    "aeskey": "00000000000000000000000000000000",
    "otp": "cccccccbdtbtvivinkunbkbgdgijtbbhbhbudgdljdvu"
    }
    ```

### OTP验证
- 请求URL：`/api/v1/otp/verify`  
- 请求方法：POST  
- 请求头：`Content-Type: application/json`
- 示例请求体：
    ```json
    {
    "serial": 123456,
    "prefix": "cccccccbdtbt",
    "uid": "000000000000",
    "aeskey": "00000000000000000000000000000000",
    "otp": "cccccccbdtbtvivinkunbkbgdgijtbbhbhbudgdljdvu"
    }
    ```
- 响应示例：
  ``` json
      {
      "status": "success"
    }
    ```

### 获取序列号
- 请求URL：`/api/v1/otp/getSerial`
- 请求方法：POST
- 请求头：`Content-Type: application/json`
- 请求体：`{"otp": "***"}`
- 响应示例：
    ```json
    {
    "status": "success",
    "data": {
        "serial": ***, # 10进制序列号
        "hex": "***"    # 十六进制序列号
    }
  }
  ```

## 注意事项

1. 确保数据库名称与配置文件一致
2. YubiKey的长按和短按是两个独立的配置，需要分别注册
3. 注册时使用的按键方式，验证时必须使用同样的方式

## status 响应说明

- `success`: 操作成功
- `error`: 设备未注册、重放攻击、操作失败、私有标识不匹配、OTP验证失败
- 更多错误码请查看响应封装中的错误码定义文件`StatusCode.java`
