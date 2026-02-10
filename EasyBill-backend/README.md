# EasyBill 后端

个人财务管理系统后端服务，基于 Spring Boot 3.4 + Spring AI (DeepSeek)。

## 技术栈

- Java 17
- Spring Boot 3.4.1
- Spring Data JPA
- Spring AI (DeepSeek 集成)
- MySQL 8.0
- Redis 7
- Maven

## 快速开始

### 1. 环境准备

```bash
# 复制环境变量配置
cp .env.example .env

# 编辑 .env 文件，填写以下信息：
# - DEEPSEEK_API_KEY: DeepSeek API 密钥
# - DB_PASSWORD: MySQL 密码
```

### 2. 启动数据库

```bash
# 使用 Docker Compose 启动 MySQL 和 Redis
cd ..
docker-compose up -d mysql redis
```

### 3. 初始化数据库

```bash
# 导入初始化 SQL
mysql -u root -p easybill < ../database_init.sql
```

### 4. 运行后端

```bash
# 使用 Maven 运行
./mvnw spring-boot:run

# 或者打包后运行
./mvnw clean package
java -jar target/easybill-backend-1.0.0-SNAPSHOT.jar
```

后端将在 `http://localhost:8080` 启动。

## API 文档

### Webhook 接口

**POST** `/api/webhook/sms`

接收 iOS 快捷指令发送的短信数据。

**Headers:**
```
Authorization: Bearer {your_api_key}
Content-Type: application/json
```

**Request Body:**
```json
{
  "rawContent": "【招商银行】您尾号1234的信用卡于12月25日12:30在全家便利店消费25.00元",
  "sender": "95555",
  "timestamp": "2026-02-08T12:30:00+08:00",
  "deviceId": "iPhone15_Pro"
}
```

**Response:**
```json
{
  "success": true,
  "message": "账单已记录",
  "transaction_id": 12345,
  "status": "PENDING",
  "parsed_data": {
    "amount": 25.00,
    "merchant": "全家便利店",
    "type": "EXPENSE"
  }
}
```

## 项目结构

```
src/main/java/com/easybill/
├── entity/          # 实体类
│   ├── User.java
│   ├── Account.java
│   ├── Category.java
│   ├── Transaction.java
│   └── RawSmsLog.java
├── repository/      # 数据访问层
├── service/         # 业务逻辑层
│   ├── AIParserService.java
│   ├── IdempotencyService.java
│   └── SmsProcessingService.java
├── controller/      # 控制器
│   └── WebhookController.java
├── dto/            # 数据传输对象
├── config/         # 配置类
└── EasyBillApplication.java
```

## 开发说明

### 获取 API Key

用户注册后，系统会自动生成唯一的 `api_key`，用于 iOS 快捷指令认证。

### AI 解析逻辑

使用 DeepSeek V3 模型解析短信，提取：
- 交易类型（收入/支出）
- 金额
- 商户名称
- 卡号尾号
- 交易时间
- 分类提示

### 幂等性保证

使用 Redis 存储幂等性键（7 天过期），防止重复记录：
```
MD5(手机尾号 + 金额 + 商户 + 原文)
```

## License

MIT
