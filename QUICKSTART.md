# EasyBill 快速启动指南

## 第一次启动

### 1. 配置环境变量

```bash
# 创建 .env 文件
cat > .env << EOF
DEEPSEEK_API_KEY=your_deepseek_api_key_here
DB_PASSWORD=easybill_password
EOF
```

### 2. 启动所有服务

```bash
# 启动 MySQL 和 Redis
docker-compose up -d mysql redis

# 等待 MySQL 启动完成（约 30 秒）
sleep 30

# 初始化数据库
docker exec -i easybill-mysql mysql -u root -peasybill_root_password easybill < EasyBill-backend/database_init.sql

# 启动后端（需要先构建）
cd EasyBill-backend
./mvnw clean package -DskipTests
cd ..
docker-compose up -d backend

# 启动前端（需要先安装依赖）
cd EasyBill-frontend
npm install
cd ..
docker-compose up -d frontend
```

### 3. 访问应用

- 前端：http://localhost:3000
- 后端 API：http://localhost:8080

## 本地开发模式

### 后端开发

```bash
cd EasyBill-backend

# 启动 MySQL 和 Redis
docker-compose up -d mysql redis

# 运行后端
./mvnw spring-boot:run
```

### 前端开发

```bash
cd EasyBill-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 常用命令

```bash
# 查看所有服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
docker-compose logs -f frontend

# 重启服务
docker-compose restart backend

# 停止所有服务
docker-compose down

# 停止并删除数据
docker-compose down -v
```

## 测试 Webhook 接口

```bash
# 创建测试用户（手动插入数据库）
docker exec -it easybill-mysql mysql -u root -peasybill_root_password easybill

# 在 MySQL 中执行：
INSERT INTO users (username, email, password_hash, api_key) VALUES
('testuser', 'test@example.com', 'hashed_password', 'test_api_key_123');

# 测试 Webhook
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

## 故障排查

### MySQL 连接失败

```bash
# 检查 MySQL 是否启动
docker-compose ps mysql

# 查看 MySQL 日志
docker-compose logs mysql

# 重启 MySQL
docker-compose restart mysql
```

### Redis 连接失败

```bash
# 检查 Redis
docker-compose ps redis

# 测试 Redis 连接
docker exec -it easybill-redis redis-cli ping
```

### 后端启动失败

```bash
# 查看后端日志
docker-compose logs backend

# 检查环境变量
docker-compose config
```
