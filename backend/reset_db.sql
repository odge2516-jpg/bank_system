USE bank_system;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE favorite_accounts;
TRUNCATE TABLE transactions;
TRUNCATE TABLE sub_accounts;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- Insert admin user with BCrypt hash for 'admin123'
-- Hash generated using BCrypt with default strength
INSERT INTO users (id, login_id, real_name, password, role, status, created_at)
VALUES ('admin001', 'admin', 'Administrator', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'admin', 'active', NOW());

-- Create default sub-account for admin
INSERT INTO sub_accounts (id, user_id, name, balance, color, created_at)
VALUES ('SUB_ADMIN', 'admin001', 'Admin Main', 1000000.00, '#3b82f6', NOW());
