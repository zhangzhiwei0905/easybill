# GitHub 安全配置计划

## 🚨 当前安全问题分析

### 1. **严重问题：敏感信息已明文暴露**

在 [`application.properties`](EasyBill-backend/src/main/resources/application.properties:24) 中发现以下敏感信息：

#### ❌ DeepSeek API Key（第 24 行）
```properties
spring.ai.openai.api-key=sk-53de728ac1c748c1abaf91ad1c2d03b3
```
**风险等级：🔴 极高**
- API Key 已明文暴露
- 如果推送到 GitHub，任何人都可以使用你的 API 配额
- 可能产生意外费用

#### ❌ JWT Secret（第 30 行）
```properties
jwt.secret=easybill_secret_key_for_jwt_token_generation_minimum_256_bits_required_for_hs256
```
**风险等级：🔴 高**
- JWT 密钥用于生成和验证用户令牌
- 泄露后攻击者可以伪造任何用户身份

#### ❌ 数据库密码（第 8 行）
```properties
spring.datasource.password=easybill_password
```
**风险等级：🟡 中**
- 虽然是开发环境密码，但不应明文提交

---

## 📋 修改计划

### 阶段 1：更新 `.gitignore`

需要确保以下文件/目录被忽略：

```gitignore
# 环境变量文件
.env
.env.local
.env.*.local
*.env

# 后端配置文件（包含敏感信息）
EasyBill-backend/src/main/resources/application.properties
EasyBill-backend/src/main/resources/application-*.properties

# 前端环境变量
EasyBill-frontend/.env
EasyBill-frontend/.env.local
EasyBill-frontend/.env.*.local

# IDE 和系统文件
.idea/
.vscode/
.DS_Store
*.iml

# 构建产物
target/
node_modules/
.next/
dist/
out/

# 日志文件
logs/
*.log

# Docker 覆盖文件
docker-compose.override.yml

# 临时文件
*.tmp
*.swp
*.bak
```

### 阶段 2：创建配置模板文件

#### 2.1 创建 `application.properties.example`
将当前的 [`application.properties`](EasyBill-backend/src/main/resources/application.properties) 复制为模板，替换敏感信息为占位符：

```properties
# DeepSeek API Key - 从环境变量读取
spring.ai.openai.api-key=${DEEPSEEK_API_KEY}

# JWT Secret - 从环境变量读取
jwt.secret=${JWT_SECRET}

# 数据库密码 - 从环境变量读取
spring.datasource.password=${DB_PASSWORD}
```

#### 2.2 创建根目录 `.env.example`
```env
# DeepSeek API Key
DEEPSEEK_API_KEY=your_deepseek_api_key_here

# 数据库密码
DB_PASSWORD=your_mysql_password

# JWT 密钥（至少 256 位）
JWT_SECRET=your_random_secret_key_here

# Redis 密码（如有）
REDIS_PASSWORD=
```

#### 2.3 创建前端 `.env.example`
```env
# API 后端地址
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### 阶段 3：修改现有配置文件

#### 3.1 修改 [`application.properties`](EasyBill-backend/src/main/resources/application.properties)
将硬编码的敏感信息替换为环境变量引用：

```properties
# 第 8 行
spring.datasource.password=${DB_PASSWORD:easybill_password}

# 第 24 行
spring.ai.openai.api-key=${DEEPSEEK_API_KEY}

# 第 30 行
jwt.secret=${JWT_SECRET}
```

#### 3.2 验证 [`docker-compose.yml`](docker-compose.yml)
已经正确使用环境变量：
- ✅ `${DB_PASSWORD}`
- ✅ `${DEEPSEEK_API_KEY}`
- ✅ `${JWT_SECRET}`

### 阶段 4：Git 历史清理（如果已推送）

如果敏感信息已经推送到 GitHub，需要：

1. **使用 git-filter-repo 或 BFG Repo-Cleaner 清理历史**
2. **强制推送清理后的历史**
3. **立即更换所有泄露的密钥**：
   - 🔴 **立即更换 DeepSeek API Key**
   - 🔴 **生成新的 JWT Secret**
   - 🟡 更改数据库密码

### 阶段 5：GitHub 仓库配置

#### 5.1 设置 GitHub Secrets（用于 CI/CD）
在 GitHub 仓库设置中添加：
- `DEEPSEEK_API_KEY`
- `JWT_SECRET`
- `DB_PASSWORD`

#### 5.2 添加 `.github/workflows` 配置（可选）
如果需要 CI/CD，确保使用 GitHub Secrets

---

## 🔒 安全最佳实践

### 1. **永远不要提交的内容**
- ❌ API Keys / Tokens
- ❌ 数据库密码
- ❌ JWT Secrets
- ❌ 私钥文件
- ❌ OAuth Client Secrets
- ❌ 第三方服务凭证

### 2. **应该提交的内容**
- ✅ `.env.example` 模板文件
- ✅ `application.properties.example` 模板
- ✅ 配置文件结构说明
- ✅ README 中的配置指南

### 3. **环境变量命名规范**
```
开发环境：.env.development
测试环境：.env.test
生产环境：.env.production（永远不提交）
```

---

## 📝 执行检查清单

- [ ] 更新 `.gitignore` 文件
- [ ] 创建 `application.properties.example` 模板
- [ ] 修改 `application.properties` 使用环境变量
- [ ] 创建根目录 `.env.example`
- [ ] 创建前端 `.env.example`
- [ ] 检查是否有其他敏感文件
- [ ] 如果已推送，清理 Git 历史
- [ ] 更换所有泄露的密钥
- [ ] 测试配置是否正常工作
- [ ] 更新 README 添加配置说明

---

## ⚠️ 紧急行动项

如果你已经将包含敏感信息的代码推送到 GitHub：

### 1. **立即更换 DeepSeek API Key**
   - 登录 DeepSeek 控制台
   - 撤销当前 API Key
   - 生成新的 API Key

### 2. **生成新的 JWT Secret**
   ```bash
   # 使用 openssl 生成随机密钥
   openssl rand -base64 64
   ```

### 3. **更改数据库密码**
   - 更新 MySQL 用户密码
   - 更新所有配置文件

---

## 📚 相关文档

- [Spring Boot 外部化配置](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [GitHub Secrets 文档](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [Git 历史清理工具 BFG](https://rtyley.github.io/bfg-repo-cleaner/)
