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

---

## 🚀 快速开始

### 前提条件

- ✅ Docker Desktop（用于运行 MySQL 和 Redis）
- ✅ Java 17+（用于编译后端）
- ✅ Node.js 20+（用于前端）
- ✅ DeepSeek API Key（[获取地址](https://platform.deepseek.com/api_keys)）

### 第一步：配置环境变量

```bash
# 克隆项目
git clone https://github.com/zhangzhiwei0905/easybill.git
cd easybill

# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，填写您的 DeepSeek API Key
# DEEPSEEK_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxx
# DB_PASSWORD=easybill_password
# JWT_SECRET=your_random_secret_key_here
```

> **⚠️ 重要**：`.env` 文件包含敏感信息，已被 `.gitignore` 保护，不会提交到 Git。

### 第二步：启动 MySQL 和 Redis

```bash
# 确保 Docker Desktop 正在运行
docker-compose up -d mysql redis

# 等待 MySQL 启动完成（约 30 秒）
sleep 30

# 初始化数据库
docker exec -i easybill-mysql mysql -u root -p${DB_PASSWORD} easybill_db < database_init.sql
```

### 第三步：启动后端

```bash
cd EasyBill-backend

# 编译并运行后端
./mvnw spring-boot:run
```

后端将在 **http://localhost:8080** 启动。

### 第四步：启动前端

**打开新的终端窗口**：

```bash
cd EasyBill-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端将在 **http://localhost:3000** 启动。

### 第五步：验证系统

访问 http://localhost:3000 查看应用。

测试 Webhook API：

```bash
curl -X POST http://localhost:8080/api/webhook/sms \
  -H "Authorization: Bearer your_api_key" \
  -H "Content-Type: application/json" \
  -d '{
    "rawContent": "【招商银行】您尾号1234的信用卡于12月25日12:30在全家便利店消费25.00元",
    "sender": "95555",
    "timestamp": "2026-02-08T12:30:00+08:00",
    "deviceId": "iPhone15_Pro"
  }'
```

---

## 📁 项目结构

```
EasyBill/
├── EasyBill-backend/              # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/easybill/
│   │   │   │   ├── controller/    # REST API 控制器
│   │   │   │   ├── service/       # 业务逻辑层
│   │   │   │   ├── entity/        # 数据库实体
│   │   │   │   └── config/        # 配置类
│   │   │   └── resources/
│   │   │       └── application.properties.example  # 配置模板
│   ├── pom.xml
│   └── Dockerfile
├── EasyBill-frontend/             # Next.js 前端
│   ├── app/
│   ├── package.json
│   └── Dockerfile
├── scripts/                       # iOS 快捷指令
├── docker-compose.yml             # Docker Compose 配置
├── .env.example                   # 环境变量模板
└── README.md                      # 本文件
```

---

## 🔒 安全配置

### 环境变量说明

项目使用环境变量管理敏感信息，**永远不要**将以下信息提交到 Git：

| 变量名 | 说明 | 获取方式 |
|--------|------|----------|
| `DEEPSEEK_API_KEY` | DeepSeek API 密钥 | [DeepSeek 控制台](https://platform.deepseek.com/api_keys) |
| `JWT_SECRET` | JWT 签名密钥 | `openssl rand -base64 64` |
| `DB_PASSWORD` | 数据库密码 | 自定义 |

### 文件说明

| 文件 | 用途 | 是否提交到 Git |
|------|------|----------------|
| `.env` | 包含真实的敏感信息 | ❌ 不提交 |
| `.env.example` | 配置模板，不含真实值 | ✅ 提交 |
| `application.properties` | 使用环境变量引用 | ❌ 不提交 |
| `application.properties.example` | 配置模板 | ✅ 提交 |

---

## 🛠️ 开发指南

### 本地开发

**后端**：
```bash
cd EasyBill-backend
./mvnw spring-boot:run
```

**前端**：
```bash
cd EasyBill-frontend
npm run dev
```

### 数据库管理

```bash
# 连接 MySQL
docker exec -it easybill-mysql mysql -u root -p

# 查看 Redis
docker exec -it easybill-redis redis-cli
```

### 常用命令

```bash
# 查看所有服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend

# 重启服务
docker-compose restart backend

# 停止所有服务
docker-compose down

# 停止并删除数据
docker-compose down -v
```

---

## 📱 配置 iOS 快捷指令

1. 在 iPhone 上打开"快捷指令" App
2. 导入 `scripts/easybill-sms-webhook.shortcut`
3. 编辑快捷指令，填写：
   - API URL：`https://your-domain.com/api/webhook/sms`
   - API Key：注册后在系统中获取

---

## 🚢 部署到生产环境

### 使用 Docker Compose

```bash
# 编辑 .env 文件，填写生产环境配置
vim .env

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f
```

### 配置 HTTPS

编辑 `nginx/nginx.conf`，配置 SSL 证书。

---

## ❓ 常见问题

### 1. AI 解析失败

- 检查 DeepSeek API Key 是否正确
- 查看后端日志：`docker-compose logs backend`

### 2. iOS 快捷指令无法触发

- 确认快捷指令中的 API URL 和 API Key 正确
- 检查网络连接（需要外网访问）

### 3. 数据库连接失败

- 确认 MySQL 容器已启动：`docker-compose ps mysql`
- 检查 `.env` 中的数据库配置

### 4. 后端启动失败

```bash
# 查看后端日志
docker-compose logs backend

# 检查环境变量
docker-compose config
```

---

## 🗺️ 路线图

- [ ] 支付宝/微信账单导出 API 集成
- [ ] 预算管理功能
- [ ] 月度报告生成
- [ ] 移动端 App

---

## 📄 License

MIT

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📞 联系方式

如有问题，请在 GitHub 上提交 Issue。
