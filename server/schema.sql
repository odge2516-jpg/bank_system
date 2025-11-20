-- 創建資料庫（如果不存在）
CREATE DATABASE IF NOT EXISTS bank_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bank_system;

-- 先刪除現有表（如果存在）以避免外鍵衝突
DROP TABLE IF EXISTS favorite_accounts;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS sub_accounts;
DROP TABLE IF EXISTS users;

-- 用戶表
CREATE TABLE users (
    id VARCHAR(12) PRIMARY KEY COMMENT '銀行帳號（12位數字）',
    login_id VARCHAR(50) NOT NULL UNIQUE COMMENT '登入帳號',
    real_name VARCHAR(100) NOT NULL COMMENT '真實姓名',
    password VARCHAR(255) NOT NULL COMMENT '密碼',
    role ENUM('user', 'admin') DEFAULT 'user' COMMENT '角色',
    status ENUM('active', 'frozen') DEFAULT 'active' COMMENT '帳戶狀態',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    INDEX idx_login_id (login_id),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用戶表';

-- 子帳戶表
CREATE TABLE sub_accounts (
    id VARCHAR(50) PRIMARY KEY COMMENT '子帳戶ID',
    user_id VARCHAR(12) NOT NULL COMMENT '所屬用戶ID',
    name VARCHAR(50) NOT NULL COMMENT '子帳戶名稱',
    balance DECIMAL(15, 2) DEFAULT 0.00 COMMENT '餘額',
    color VARCHAR(7) DEFAULT '#3b82f6' COMMENT '顏色代碼',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='子帳戶表';

-- 交易記錄表
CREATE TABLE transactions (
    id VARCHAR(50) PRIMARY KEY COMMENT '交易ID',
    user_id VARCHAR(12) NOT NULL COMMENT '用戶ID',
    type VARCHAR(20) NOT NULL COMMENT '交易類型',
    amount DECIMAL(15, 2) NOT NULL COMMENT '金額',
    note TEXT COMMENT '備註',
    time VARCHAR(50) NOT NULL COMMENT '交易時間（格式化字串）',
    timestamp BIGINT NOT NULL COMMENT '時間戳',
    sub_account_id VARCHAR(50) COMMENT '關聯的子帳戶ID',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (sub_account_id) REFERENCES sub_accounts(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易記錄表';

-- 常用帳號表（關聯表）
CREATE TABLE favorite_accounts (
    user_id VARCHAR(12) NOT NULL COMMENT '用戶ID',
    favorite_user_id VARCHAR(12) NOT NULL COMMENT '常用帳號的用戶ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入時間',
    PRIMARY KEY (user_id, favorite_user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (favorite_user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='常用帳號表';

-- 插入預設管理員帳號
INSERT INTO users (id, login_id, real_name, password, role, status, created_at)
VALUES ('ADMIN000000', 'admin', '系統管理員', 'admin123', 'admin', 'active', NOW())
ON DUPLICATE KEY UPDATE login_id = login_id;
