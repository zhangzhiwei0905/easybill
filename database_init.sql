-- EasyBill 数据库初始化脚本
-- MySQL 8.0+

-- ============================================
-- 1. 创建数据库
-- ============================================
CREATE DATABASE IF NOT EXISTS easybill_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE easybill_db;

-- ============================================
-- 2. 用户表
-- ============================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    api_key VARCHAR(64) NOT NULL UNIQUE COMMENT 'Webhook 认证密钥',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_api_key (api_key),
    INDEX idx_email (email),
    INDEX idx_phone (phone)
) ENGINE=InnoDB COMMENT='用户表';

-- ============================================
-- 3. 账户表
-- ============================================
CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '所属用户',
    account_name VARCHAR(50) NOT NULL COMMENT '账户名称',
    account_type VARCHAR(20) NOT NULL COMMENT '账户类型: BANK_CARD/ALIPAY/WECHAT/CASH',
    last_four_digits VARCHAR(4) COMMENT '卡号尾号',
    source_identifier VARCHAR(50) COMMENT '支付来源标识',
    balance DECIMAL(18,2) DEFAULT 0.00 COMMENT '账户余额',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_account_type (account_type)
) ENGINE=InnoDB COMMENT='账户表';

-- ============================================
-- 4. 分类表
-- ============================================
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '所属用户（NULL 表示系统预置）',
    parent_id BIGINT COMMENT '父分类 ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(100) COMMENT '图标标识',
    type VARCHAR(10) NOT NULL COMMENT '类型: INCOME/EXPENSE',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统预置',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_type (type)
) ENGINE=InnoDB COMMENT='分类表';

-- ============================================
-- 5. 交易表
-- ============================================
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '所属用户',
    account_id BIGINT COMMENT '关联账户',
    category_id BIGINT COMMENT '关联分类',
    type VARCHAR(10) NOT NULL COMMENT '类型: INCOME/EXPENSE',
    amount DECIMAL(18,2) NOT NULL COMMENT '金额（正数）',
    merchant VARCHAR(100) COMMENT '商户名称',
    transaction_time DATETIME NOT NULL COMMENT '交易时间',
    raw_log_id BIGINT COMMENT '关联原始短信',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/CONFIRMED/MANUAL',
    remark VARCHAR(255) COMMENT '用户备注',
    idempotency_key VARCHAR(64) UNIQUE COMMENT '幂等键',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE SET NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_transaction_time (transaction_time),
    INDEX idx_status (status),
    INDEX idx_type (type),
    INDEX idx_idempotency_key (idempotency_key)
) ENGINE=InnoDB COMMENT='交易表';

-- ============================================
-- 6. 原始短信日志表
-- ============================================
CREATE TABLE raw_sms_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '所属用户',
    sender VARCHAR(20) NOT NULL COMMENT '发件人号码',
    full_content TEXT NOT NULL COMMENT '短信全文',
    ai_response TEXT COMMENT 'AI 解析结果 JSON',
    device_info VARCHAR(50) COMMENT '设备标识',
    parse_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '解析状态: PENDING/SUCCESS/FAILED',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_sender (sender),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB COMMENT='原始短信日志表';

-- ============================================
-- 7. 短信来源配置表
-- ============================================
CREATE TABLE sms_sources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '所属用户',
    sender_number VARCHAR(20) NOT NULL COMMENT '短信号码',
    bank_name VARCHAR(50) NOT NULL COMMENT '银行名称',
    prompt_template_id BIGINT COMMENT '关联 Prompt 模板',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_sender_number (sender_number)
) ENGINE=InnoDB COMMENT='短信来源配置表';

-- ============================================
-- 8. Prompt 模板表
-- ============================================
CREATE TABLE prompt_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_type VARCHAR(50) NOT NULL UNIQUE COMMENT '银行类型',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_content TEXT NOT NULL COMMENT 'Prompt 模板内容',
    example_sms TEXT COMMENT '示例短信',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认模板',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_bank_type (bank_type)
) ENGINE=InnoDB COMMENT='Prompt 模板表';

-- ============================================
-- 9. 预置数据：系统分类
-- ============================================

-- 支出分类
INSERT INTO categories (user_id, parent_id, name, icon, type, is_system, sort_order) VALUES
-- 一级分类
(NULL, NULL, '餐饮', '🍔', 'EXPENSE', 1, 1),
(NULL, NULL, '交通', '🚗', 'EXPENSE', 1, 2),
(NULL, NULL, '购物', '🛍️', 'EXPENSE', 1, 3),
(NULL, NULL, '娱乐', '🎮', 'EXPENSE', 1, 4),
(NULL, NULL, '医疗', '🏥', 'EXPENSE', 1, 5),
(NULL, NULL, '住房', '🏠', 'EXPENSE', 1, 6),
(NULL, NULL, '教育', '📚', 'EXPENSE', 1, 7),
(NULL, NULL, '通讯', '📱', 'EXPENSE', 1, 8),
(NULL, NULL, '其他支出', '💸', 'EXPENSE', 1, 99);

-- 二级分类（餐饮）
INSERT INTO categories (user_id, parent_id, name, icon, type, is_system, sort_order) 
SELECT NULL, id, '早餐', '🥐', 'EXPENSE', 1, 1 FROM categories WHERE name = '餐饮' AND parent_id IS NULL;
INSERT INTO categories (user_id, parent_id, name, icon, type, is_system, sort_order) 
SELECT NULL, id, '午餐', '🍱', 'EXPENSE', 1, 2 FROM categories WHERE name = '餐饮' AND parent_id IS NULL;
INSERT INTO categories (user_id, parent_id, name, icon, type, is_system, sort_order) 
SELECT NULL, id, '晚餐', '🍜', 'EXPENSE', 1, 3 FROM categories WHERE name = '餐饮' AND parent_id IS NULL;
INSERT INTO categories (user_id, parent_id, name, icon, type, is_system, sort_order) 
SELECT NULL, id, '便利店', '🏪', 'EXPENSE', 1, 4 FROM categories WHERE name = '餐饮' AND parent_id IS NULL;
INSERT INTO categories (user_id, parent_id, name, icon, type, is_system, sort_order) 
SELECT NULL, id, '咖啡饮品', '☕', 'EXPENSE', 1, 5 FROM categories WHERE name = '餐饮' AND parent_id IS NULL;

-- 二级分类（交通）
INSERT INTO categories (user_id, parent_id, name, icon, type, is_system, sort_order) 
SELECT NULL, id, '地铁', '🚇', 'EXPENSE', 1, 1 FROM categories WHERE name = '交通' AND parent_id IS NULL;
INSERT INTO categories (user_id, parent_id, name, icon, type, is_system, sort_order) 
SELECT NULL, id, '公交', '🚌', 'EXPENSE', 1, 2 FROM categories WHERE name = '交通' AND parent_id IS NULL;
INSERT INTO categories (user_id, parent_id, name, icon, type, is_system, sort_order) 
SELECT NULL, id, '打车', '🚕', 'EXPENSE', 1, 3 FROM categories WHERE name = '交通' AND parent_id IS NULL;
INSERT INTO categories (user_id, parent_id, name, icon, type, is_system, sort_order) 
SELECT NULL, id, '加油', '⛽', 'EXPENSE', 1, 4 FROM categories WHERE name = '交通' AND parent_id IS NULL;

-- 二级分类（购物）
INSERT INTO categories (user_id, parent_id, name, icon, type, is_system, sort_order) 
SELECT NULL, id, '服饰', '👔', 'EXPENSE', 1, 1 FROM categories WHERE name = '购物' AND parent_id IS NULL;
INSERT INTO categories (user_id, parent_id, name, icon, type, is_system, sort_order) 
SELECT NULL, id, '数码', '💻', 'EXPENSE', 1, 2 FROM categories WHERE name = '购物' AND parent_id IS NULL;
INSERT INTO categories (user_id, parent_id, name, icon, type, is_system, sort_order) 
SELECT NULL, id, '日用品', '🧴', 'EXPENSE', 1, 3 FROM categories WHERE name = '购物' AND parent_id IS NULL;
INSERT INTO categories (user_id, parent_id, name, icon, type, is_system, sort_order) 
SELECT NULL, id, '超市', '🛒', 'EXPENSE', 1, 4 FROM categories WHERE name = '购物' AND parent_id IS NULL;

-- 收入分类
INSERT INTO categories (user_id, parent_id, name, icon, type, is_system, sort_order) VALUES
(NULL, NULL, '工资', '💰', 'INCOME', 1, 1),
(NULL, NULL, '奖金', '🎁', 'INCOME', 1, 2),
(NULL, NULL, '投资收益', '📈', 'INCOME', 1, 3),
(NULL, NULL, '兼职', '💼', 'INCOME', 1, 4),
(NULL, NULL, '其他收入', '💵', 'INCOME', 1, 99);

-- ============================================
-- 10. 预置数据：Prompt 模板
-- ============================================

-- 招商银行模板
INSERT INTO prompt_templates (bank_type, template_name, template_content, example_sms, is_default) VALUES
('CMB', '招商银行信用卡', 
'你是一个专业的银行短信解析助手。请从以下招商银行短信中提取交易信息，返回 JSON 格式：
{
  "type": "INCOME 或 EXPENSE",
  "amount": "金额（数字）",
  "merchant": "商户名称",
  "card_last_four": "卡号尾号",
  "transaction_time": "交易时间（ISO 8601 格式）",
  "category_hint": "分类提示（如：餐饮、交通）"
}

注意：
1. 金额必须是正数
2. 如果短信中提到"消费"、"支出"，type 为 EXPENSE
3. 如果短信中提到"存入"、"到账"，type 为 INCOME
4. 商户名称尽量完整提取
5. 如果无法提取某个字段，返回 null',
'【招商银行】您尾号1234的信用卡于12月25日12:30在全家便利店消费25.00元',
1);

-- 工商银行模板
INSERT INTO prompt_templates (bank_type, template_name, template_content, example_sms, is_default) VALUES
('ICBC', '工商银行借记卡',
'你是一个专业的银行短信解析助手。请从以下工商银行短信中提取交易信息，返回 JSON 格式：
{
  "type": "INCOME 或 EXPENSE",
  "amount": "金额（数字）",
  "merchant": "商户名称",
  "card_last_four": "卡号尾号",
  "transaction_time": "交易时间（ISO 8601 格式）",
  "category_hint": "分类提示"
}

注意：工商银行短信格式为"您尾号XXXX的账户于XX月XX日XX:XX支出/收入XX元"',
'【工商银行】您尾号5678的账户于12月25日14:20支出128.50元',
0);

-- 支付宝模板
INSERT INTO prompt_templates (bank_type, template_name, template_content, example_sms, is_default) VALUES
('ALIPAY', '支付宝',
'你是一个专业的支付宝短信解析助手。请从以下支付宝短信中提取交易信息，返回 JSON 格式：
{
  "type": "INCOME 或 EXPENSE",
  "amount": "金额（数字）",
  "merchant": "商户名称",
  "transaction_time": "交易时间（ISO 8601 格式）",
  "category_hint": "分类提示"
}

注意：
1. 支付宝短信通常包含"支付成功"、"收款"等关键词
2. 商户名称可能在"向XX付款"或"在XX消费"中',
'【支付宝】您在星巴克支付成功，金额45.00元',
0);

-- 默认通用模板
INSERT INTO prompt_templates (bank_type, template_name, template_content, example_sms, is_default) VALUES
('DEFAULT', '通用模板',
'你是一个专业的银行短信解析助手。请从以下短信中提取交易信息，返回 JSON 格式：
{
  "type": "INCOME 或 EXPENSE",
  "amount": "金额（数字）",
  "merchant": "商户名称",
  "card_last_four": "卡号尾号（如有）",
  "transaction_time": "交易时间（ISO 8601 格式）",
  "category_hint": "分类提示"
}

如果无法确定某个字段，返回 null。',
NULL,
1);

-- ============================================
-- 11. 创建测试用户（可选）
-- ============================================
-- 密码：admin123（BCrypt 加密后）
INSERT INTO users (username, email, phone, password_hash, api_key) VALUES
('admin', 'admin@easybill.com', '13800138000', 
'$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM4JzjCsHW8Jv.5b8Sxe',
'eb_test_key_1234567890abcdef');

-- 为测试用户创建默认账户
INSERT INTO accounts (user_id, account_name, account_type, last_four_digits) VALUES
(1, '招商银行信用卡', 'BANK_CARD', '1234'),
(1, '支付宝余额', 'ALIPAY', NULL),
(1, '微信余额', 'WECHAT', NULL);

-- ============================================
-- 完成
-- ============================================
