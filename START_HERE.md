# 🚀 EasyBill 本地启动指南（无需本地 MySQL）

## 前提条件

1. ✅ 已安装 Docker Desktop（Mac 版本）
2. ✅ 已安装 Java 17+（用于编译后端）
3. ✅ 已安装 Node.js 20+（用于前端）
4. ✅ 已获取 DeepSeek API Key

## 第一步：配置环境变量

### 1. 编辑 `.env` 文件

项目根目录下已经创建了 `.env` 文件，请编辑它：

```bash
# 在项目根目录
cd /Users/zhangzhiwei/Documents/antigravityProjects/EasyBill

# 使用编辑器打开 .env
open -e .env
```

**修改以下内容：**

```env
# 替换为您的 DeepSeek API Key
DEEPSEEK_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxx

# 数据库密码（可以保持默认，或修改为您想要的密码）
DB_PASSWORD=easybill_password
```

> **注意**：`DB_PASSWORD` 是 Docker 容器内 MySQL 的密码，不需要您本地安装 MySQL。

## 第二步：启动 MySQL 和 Redis（Docker）

```bash
# 确保 Docker Desktop 正在运行

# 启动 MySQL 和 Redis 容器
docker-compose up -d mysql redis

# 查看容器状态（应该显示 healthy）
docker-compose ps
```

**预期输出：**
```
NAME                IMAGE         STATUS
easybill-mysql      mysql:8.0     Up (healthy)
easybill-redis      redis:7       Up (healthy)
```

## 第三步：初始化数据库

等待 MySQL 启动完成（约 30 秒），然后导入初始化 SQL：

```bash
# 导入数据库结构和预置数据
docker exec -i easybill-mysql mysql -u root -peasybill_root_password easybill < EasyBill-backend/database_init.sql

# 验证数据库是否初始化成功
docker exec -it easybill-mysql mysql -u root -peasybill_root_password -e "USE easybill; SHOW TABLES;"
```

**预期输出：**
```
+--------------------+
| Tables_in_easybill |
+--------------------+
| accounts           |
| categories         |
| prompt_templates   |
| raw_sms_logs       |
| sms_sources        |
| transactions       |
| users              |
+--------------------+
```

## 第四步：启动后端（本地开发模式）

```bash
cd EasyBill-backend

# 编译并运行后端
./mvnw spring-boot:run
```

**预期输出：**
```
Started EasyBillApplication in X.XXX seconds
```

后端将在 **http://localhost:8080** 启动。

## 第五步：启动前端（本地开发模式）

**打开新的终端窗口**，然后：

```bash
cd /Users/zhangzhiwei/Documents/antigravityProjects/EasyBill/EasyBill-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

**预期输出：**
```
▲ Next.js 16.x.x
- Local:        http://localhost:3000
```

前端将在 **http://localhost:3000** 启动。

## 第六步：验证系统

### 1. 访问前端

打开浏览器访问：http://localhost:3000

您应该看到 EasyBill 的欢迎页面。

### 2. 测试 Webhook API

在终端中运行：

```bash
curl -X POST http://localhost:8080/api/webhook/sms \
  -H "Authorization: Bearer test_api_key_123" \
  -H "Content-Type: application/json" \
  -d '{
    "rawContent": "【招商银行】您尾号1234的信用卡于12月25日12:30在全家便利店消费25.00元",
    "sender": "95555",
    "timestamp": "2026-02-08T12:30:00+08:00",
    "deviceId": "iPhone15_Pro"
  }'
```

**预期响应：**
```json
{
  "success": true,
  "message": "账单已记录",
  "transaction_id": 1,
  "status": "PENDING",
  "parsed_data": {
    "amount": 25.00,
    "merchant": "全家便利店",
    "type": "EXPENSE"
  }
}
```

### 3. 查看数据库记录

```bash
docker exec -it easybill-mysql mysql -u root -peasybill_root_password easybill -e "SELECT * FROM transactions;"
```

您应该看到刚才创建的交易记录。

## 常见问题

### Q1: Docker 容器启动失败

**解决方案：**
```bash
# 查看日志
docker-compose logs mysql

# 重启容器
docker-compose restart mysql
```

### Q2: 后端启动失败（无法连接数据库）

**解决方案：**
```bash
# 确认 MySQL 容器正在运行
docker-compose ps mysql

# 检查后端配置文件
cat EasyBill-backend/src/main/resources/application.properties
```

### Q3: Maven 构建失败

**解决方案：**
```bash
# 清理并重新构建
cd EasyBill-backend
./mvnw clean install -DskipTests
```

### Q4: 前端依赖安装失败

**解决方案：**
```bash
cd EasyBill-frontend

# 删除 node_modules 和 package-lock.json
rm -rf node_modules package-lock.json

# 重新安装
npm install
```

## 停止服务

### 停止后端和前端
在各自的终端窗口按 `Ctrl + C`

### 停止 Docker 容器
```bash
docker-compose down

# 如果想删除数据（重新开始）
docker-compose down -v
```

## 下一步

系统启动成功后，您可以：
1. 测试 Webhook API 的 AI 解析功能
2. 查看数据库中的预置分类
3. 准备配置 iOS 快捷指令

如有问题，请查看日志或联系开发者！🚀
