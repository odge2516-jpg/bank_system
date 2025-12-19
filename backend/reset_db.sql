USE bank_system;
SET FOREIGN_KEY_CHECKS = 0;

-- Clear transactions and favorite accounts
TRUNCATE TABLE favorite_accounts;
TRUNCATE TABLE transactions;

-- Clear sub-accounts except for admin
DELETE FROM sub_accounts WHERE user_id <> 'admin001';

-- Clear users except for admin
DELETE FROM users WHERE id <> 'admin001';

SET FOREIGN_KEY_CHECKS = 1;

-- Ensure admin sub-account balance is integer
UPDATE sub_accounts SET balance = 1000000 WHERE id = 'SUB_ADMIN';
