# EasyBill

个人财务管理系统 - 基于 AI 的智能账单管理工具

## 项目简介

EasyBill 是一款支持自动采集、AI 解析和数据可视化的个人财务管理系统。通过 iOS 快捷指令监听银行短信，自动记录账单信息，并提供多维度的数据分析。

### 核心特性

- ✅ **自动采集**：iOS 快捷指令监听短信，自动上传账单
- ✅ **AI 解析**：DeepSeek V3 智能识别交易信息
- ✅ **幂等性保证**：防止重复记录
- ✅ **多租户架构**：支持多用户独立使用
- ✅ **数据可视化**：Echarts 图表展示
- ✅ **Docker 部署**：一键启动所有服务

## 技术栈

### 后端
- Java 17
- Spring Boot 3.4
- Spring AI (DeepSeek)
- MySQL 8.0
- Redis 7

### 前端
- Next.js 16
- React 19
- Tailwind CSS 4
- Echarts

### 部署
- Docker Compose
- Nginx

## 快速开始

### 1. 环境准备

```bash
# 克隆项目
git clone <repository-url>
cd EasyBill

# 复制环境变量配置
cp .env.example .env

# 编辑 .env 文件，填写 DeepSeek API Key
```

### 2. 启动服务

```bash
# 使用 Docker Compose 一键启动
docker-compose up -d

# 查看日志
docker-compose logs -f
```

服务启动后：
- 前端：http://localhost:3000
- 后端 API：http://localhost:8080
- Nginx：http://localhost

### 3. 初始化数据库

```bash
# 导入初始化 SQL（包含预置分类和 Prompt 模板）
docker exec -i easybill-mysql mysql -u root -peasybill_root_password easybill < EasyBill-backend/database_init.sql
```

### 4. 配置 iOS 快捷指令

1. 在 iPhone 上打开"快捷指令" App
2. 导入 `scripts/easybill-sms-webhook.shortcut`
3. 编辑快捷指令，填写：
   - API URL：`https://your-domain.com/api/webhook/sms`
   - API Key：注册后在系统中获取

## 项目结构

```
EasyBill/
├── EasyBill-backend/       # Spring Boot 后端
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── EasyBill-frontend/      # Next.js 前端
│   ├── app/
│   ├── package.json
│   └── Dockerfile
├── scripts/                # iOS 快捷指令
├── nginx/                  # Nginx 配置
├── docker-compose.yml      # Docker Compose 配置
└── .env.example           # 环境变量示例
```

## 开发指南

### 本地开发

**后端**：
```bash
cd EasyBill-backend
./mvnw spring-boot:run
```

**前端**：
```bash
cd EasyBill-frontend
npm install
npm run dev
```

### 数据库管理

```bash
# 连接 MySQL
docker exec -it easybill-mysql mysql -u root -p

# 查看 Redis
docker exec -it easybill-redis redis-cli
```

## API 文档

### Webhook 接口

**POST** `/api/webhook/sms`

接收 iOS 快捷指令发送的短信数据。

**Headers:**
```
Authorization: Bearer {your_api_key}
```

**Request:**
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
  "parsed_data": {
    "amount": 25.00,
    "merchant": "全家便利店",
    "type": "EXPENSE"
  }
}
```

## 部署到云服务器

### 1. 准备服务器

```bash
# 安装 Docker 和 Docker Compose
curl -fsSL https://get.docker.com | sh
```

### 2. 配置域名和 HTTPS

编辑 `nginx/nginx.conf`，配置 SSL 证书。

### 3. 启动服务

```bash
docker-compose up -d
```

## 常见问题

### 1. AI 解析失败

- 检查 DeepSeek API Key 是否正确
- 查看后端日志：`docker-compose logs backend`

### 2. iOS 快捷指令无法触发

- 确认快捷指令中的 API URL 和 API Key 正确
- 检查网络连接（需要外网访问）

### 3. 数据库连接失败

- 确认 MySQL 容器已启动
- 检查 `docker-compose.yml` 中的数据库配置

## 路线图

- [ ] 支付宝/微信账单导出 API 集成
- [ ] 预算管理功能
- [ ] 月度报告生成
- [ ] 移动端 App

## License

MIT

## 贡献

欢迎提交 Issue 和 Pull Request！
