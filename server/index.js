const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const db = require('./config/database');
require('dotenv').config();

const app = express();
const PORT = process.env.SERVER_PORT || 3000;

// Middleware
app.use(cors());
app.use(bodyParser.json());

// ===== 用戶相關 API =====

// 註冊
app.post('/api/register', async (req, res) => {
    const { realName, loginId, password, initialDeposit } = req.body;

    try {
        // 檢查登入帳號是否已存在
        const [existing] = await db.query('SELECT id FROM users WHERE login_id = ?', [loginId]);
        if (existing.length > 0) {
            return res.status(400).json({ error: '此登入帳號已被註冊' });
        }

        // 生成12位數銀行帳號
        let bankAccountNumber;
        let attempts = 0;
        do {
            const part1 = Math.floor(1000 + Math.random() * 9000);
            const part2 = Math.floor(1000 + Math.random() * 9000);
            const part3 = Math.floor(1000 + Math.random() * 9000);
            bankAccountNumber = `${part1}${part2}${part3}`;

            const [check] = await db.query('SELECT id FROM users WHERE id = ?', [bankAccountNumber]);
            if (check.length === 0) break;

            attempts++;
            if (attempts > 100) {
                return res.status(500).json({ error: '無法生成唯一帳號' });
            }
        } while (true);

        // 創建用戶
        await db.query(
            'INSERT INTO users (id, login_id, real_name, password, role, status) VALUES (?, ?, ?, ?, ?, ?)',
            [bankAccountNumber, loginId, realName, password, 'user', 'active']
        );

        // 創建主子帳戶
        const subAccountId = 'SUB001_' + bankAccountNumber;
        await db.query(
            'INSERT INTO sub_accounts (id, user_id, name, balance, color) VALUES (?, ?, ?, ?, ?)',
            [subAccountId, bankAccountNumber, '主帳戶', initialDeposit, '#3b82f6']
        );

        res.json({
            success: true,
            user: {
                id: bankAccountNumber,
                loginId,
                realName,
                role: 'user',
                status: 'active'
            }
        });
    } catch (error) {
        console.error('註冊錯誤:', error);
        res.status(500).json({ error: '註冊失敗' });
    }
});

// 登入
app.post('/api/login', async (req, res) => {
    const { loginId, password } = req.body;

    try {
        const [users] = await db.query(
            'SELECT * FROM users WHERE login_id = ? AND password = ?',
            [loginId, password]
        );

        if (users.length === 0) {
            return res.status(401).json({ error: '帳號或密碼錯誤' });
        }

        const user = users[0];

        // 檢查帳戶狀態（管理員除外）
        if (user.role !== 'admin' && user.status === 'frozen') {
            return res.status(403).json({ error: '帳戶已被凍結，請聯繫管理員' });
        }

        // 獲取子帳戶
        const [subAccounts] = await db.query(
            'SELECT * FROM sub_accounts WHERE user_id = ? ORDER BY created_at',
            [user.id]
        );

        // 獲取常用帳號
        const [favoriteAccounts] = await db.query(
            'SELECT favorite_user_id FROM favorite_accounts WHERE user_id = ?',
            [user.id]
        );

        res.json({
            success: true,
            user: {
                id: user.id,
                loginId: user.login_id,
                realName: user.real_name,
                role: user.role,
                status: user.status,
                createdAt: user.created_at,
                subAccounts: subAccounts.map(sub => ({
                    id: sub.id,
                    name: sub.name,
                    balance: parseFloat(sub.balance),
                    color: sub.color
                })),
                favoriteAccounts: favoriteAccounts.map(f => f.favorite_user_id)
            }
        });
    } catch (error) {
        console.error('登入錯誤:', error);
        res.status(500).json({ error: '登入失敗' });
    }
});

// 獲取用戶資料（含子帳戶）
app.get('/api/user/:userId', async (req, res) => {
    const { userId } = req.params;

    try {
        const [users] = await db.query('SELECT * FROM users WHERE id = ?', [userId]);
        if (users.length === 0) {
            return res.status(404).json({ error: '用戶不存在' });
        }

        const user = users[0];

        // 獲取子帳戶
        const [subAccounts] = await db.query(
            'SELECT * FROM sub_accounts WHERE user_id = ? ORDER BY created_at',
            [userId]
        );

        // 獲取常用帳號
        const [favoriteAccounts] = await db.query(
            'SELECT favorite_user_id FROM favorite_accounts WHERE user_id = ?',
            [userId]
        );

        res.json({
            id: user.id,
            loginId: user.login_id,
            realName: user.real_name,
            role: user.role,
            status: user.status,
            createdAt: user.created_at,
            subAccounts: subAccounts.map(sub => ({
                id: sub.id,
                name: sub.name,
                balance: parseFloat(sub.balance),
                color: sub.color
            })),
            favoriteAccounts: favoriteAccounts.map(f => f.favorite_user_id)
        });
    } catch (error) {
        console.error('獲取用戶錯誤:', error);
        res.status(500).json({ error: '獲取用戶資料失敗' });
    }
});

// ===== 交易相關 API =====

// 存款
app.post('/api/deposit', async (req, res) => {
    const { userId, amount, subAccountId } = req.body;

    try {
        const [subAccounts] = await db.query(
            'SELECT * FROM sub_accounts WHERE id = ? AND user_id = ?',
            [subAccountId, userId]
        );

        if (subAccounts.length === 0) {
            return res.status(404).json({ error: '子帳戶不存在' });
        }

        const subAccount = subAccounts[0];
        const newBalance = parseFloat(subAccount.balance) + amount;

        // 更新子帳戶餘額
        await db.query(
            'UPDATE sub_accounts SET balance = ? WHERE id = ?',
            [newBalance, subAccountId]
        );

        // 記錄交易
        const transactionId = 'TXN' + Date.now() + Math.random();
        const time = new Date().toLocaleString('zh-TW');
        await db.query(
            'INSERT INTO transactions (id, user_id, type, amount, note, time, timestamp, sub_account_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
            [transactionId, userId, '存款', amount, `存入「${subAccount.name}」`, time, Date.now(), subAccountId]
        );

        res.json({ success: true });
    } catch (error) {
        console.error('存款錯誤:', error);
        res.status(500).json({ error: '存款失敗' });
    }
});

// 提款
app.post('/api/withdraw', async (req, res) => {
    const { userId, amount, subAccountId } = req.body;

    try {
        const [subAccounts] = await db.query(
            'SELECT * FROM sub_accounts WHERE id = ? AND user_id = ?',
            [subAccountId, userId]
        );

        if (subAccounts.length === 0) {
            return res.status(404).json({ error: '子帳戶不存在' });
        }

        const subAccount = subAccounts[0];
        if (parseFloat(subAccount.balance) < amount) {
            return res.status(400).json({ error: '子帳戶餘額不足' });
        }

        const newBalance = parseFloat(subAccount.balance) - amount;

        // 更新子帳戶餘額
        await db.query(
            'UPDATE sub_accounts SET balance = ? WHERE id = ?',
            [newBalance, subAccountId]
        );

        // 記錄交易
        const transactionId = 'TXN' + Date.now() + Math.random();
        const time = new Date().toLocaleString('zh-TW');
        await db.query(
            'INSERT INTO transactions (id, user_id, type, amount, note, time, timestamp, sub_account_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
            [transactionId, userId, '提款', -amount, `從「${subAccount.name}」提領`, time, Date.now(), subAccountId]
        );

        res.json({ success: true });
    } catch (error) {
        console.error('提款錯誤:', error);
        res.status(500).json({ error: '提款失敗' });
    }
});

// 轉帳
app.post('/api/transfer', async (req, res) => {
    const { userId, recipientAccountNumber, amount, saveAsFavorite } = req.body;

    try {
        // 清理帳號格式
        const cleanAccountNumber = recipientAccountNumber.replace(/[-\s]/g, '');

        // 檢查收款人
        const [recipients] = await db.query('SELECT * FROM users WHERE id = ?', [cleanAccountNumber]);
        if (recipients.length === 0) {
            return res.status(404).json({ error: '收款帳號不存在' });
        }

        if (cleanAccountNumber === userId) {
            return res.status(400).json({ error: '不能轉帳給自己' });
        }

        // 獲取付款人的第一個子帳戶（主帳戶）
        const [senderSubAccounts] = await db.query(
            'SELECT * FROM sub_accounts WHERE user_id = ? ORDER BY created_at LIMIT 1',
            [userId]
        );

        if (senderSubAccounts.length === 0) {
            return res.status(404).json({ error: '找不到付款帳戶' });
        }

        const senderSubAccount = senderSubAccounts[0];
        if (parseFloat(senderSubAccount.balance) < amount) {
            return res.status(400).json({ error: '餘額不足' });
        }

        // 獲取收款人的第一個子帳戶（主帳戶）
        const [recipientSubAccounts] = await db.query(
            'SELECT * FROM sub_accounts WHERE user_id = ? ORDER BY created_at LIMIT 1',
            [cleanAccountNumber]
        );

        if (recipientSubAccounts.length === 0) {
            return res.status(404).json({ error: '找不到收款帳戶' });
        }

        const recipientSubAccount = recipientSubAccounts[0];

        // 更新付款人餘額
        const newSenderBalance = parseFloat(senderSubAccount.balance) - amount;
        await db.query(
            'UPDATE sub_accounts SET balance = ? WHERE id = ?',
            [newSenderBalance, senderSubAccount.id]
        );

        // 更新收款人餘額
        const newRecipientBalance = parseFloat(recipientSubAccount.balance) + amount;
        await db.query(
            'UPDATE sub_accounts SET balance = ? WHERE id = ?',
            [newRecipientBalance, recipientSubAccount.id]
        );

        // 加入常用帳號
        if (saveAsFavorite) {
            await db.query(
                'INSERT IGNORE INTO favorite_accounts (user_id, favorite_user_id) VALUES (?, ?)',
                [userId, cleanAccountNumber]
            );
        }

        // 記錄交易
        const time = new Date().toLocaleString('zh-TW');
        const maskAccount = (acc) => acc.slice(0, 4) + '****' + acc.slice(-4);

        const senderTxnId = 'TXN' + Date.now() + Math.random();
        await db.query(
            'INSERT INTO transactions (id, user_id, type, amount, note, time, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)',
            [senderTxnId, userId, '轉帳支出', -amount, `轉給 ${maskAccount(cleanAccountNumber)}`, time, Date.now()]
        );

        const recipientTxnId = 'TXN' + Date.now() + Math.random() + 1;
        await db.query(
            'INSERT INTO transactions (id, user_id, type, amount, note, time, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)',
            [recipientTxnId, cleanAccountNumber, '轉帳收入', amount, `來自 ${maskAccount(userId)}`, time, Date.now()]
        );

        res.json({ success: true });
    } catch (error) {
        console.error('轉帳錯誤:', error);
        res.status(500).json({ error: '轉帳失敗' });
    }
});

// 獲取交易記錄
app.get('/api/transactions/:userId', async (req, res) => {
    const { userId } = req.params;

    try {
        const [transactions] = await db.query(
            'SELECT * FROM transactions WHERE user_id = ? ORDER BY timestamp DESC',
            [userId]
        );

        res.json(transactions.map(t => ({
            id: t.id,
            userId: t.user_id,
            type: t.type,
            amount: parseFloat(t.amount),
            note: t.note,
            time: t.time,
            timestamp: t.timestamp
        })));
    } catch (error) {
        console.error('獲取交易記錄錯誤:', error);
        res.status(500).json({ error: '獲取交易記錄失敗' });
    }
});

// ===== 子帳戶相關 API =====

// 創建子帳戶
app.post('/api/sub-accounts', async (req, res) => {
    const { userId, name, color } = req.body;

    try {
        const subAccountId = 'SUB' + Date.now();
        await db.query(
            'INSERT INTO sub_accounts (id, user_id, name, balance, color) VALUES (?, ?, ?, ?, ?)',
            [subAccountId, userId, name || '新帳戶', 0, color || '#3b82f6']
        );

        res.json({
            success: true,
            subAccount: {
                id: subAccountId,
                name: name || '新帳戶',
                balance: 0,
                color: color || '#3b82f6'
            }
        });
    } catch (error) {
        console.error('創建子帳戶錯誤:', error);
        res.status(500).json({ error: '創建子帳戶失敗' });
    }
});

// 更新子帳戶名稱
app.put('/api/sub-accounts/:subAccountId', async (req, res) => {
    const { subAccountId } = req.params;
    const { name } = req.body;

    try {
        await db.query(
            'UPDATE sub_accounts SET name = ? WHERE id = ?',
            [name, subAccountId]
        );

        res.json({ success: true });
    } catch (error) {
        console.error('更新子帳戶錯誤:', error);
        res.status(500).json({ error: '更新子帳戶失敗' });
    }
});

// 刪除子帳戶
app.delete('/api/sub-accounts/:subAccountId', async (req, res) => {
    const { subAccountId } = req.params;
    const { userId } = req.query;

    try {
        // 檢查是否為最後一個子帳戶
        const [subAccounts] = await db.query(
            'SELECT COUNT(*) as count FROM sub_accounts WHERE user_id = ?',
            [userId]
        );

        if (subAccounts[0].count <= 1) {
            return res.status(400).json({ error: '至少需要保留一個子帳戶' });
        }

        // 檢查餘額
        const [account] = await db.query('SELECT balance FROM sub_accounts WHERE id = ?', [subAccountId]);
        if (account.length > 0 && parseFloat(account[0].balance) > 0) {
            return res.status(400).json({ error: '請先將此帳戶餘額轉出或提領完畢' });
        }

        await db.query('DELETE FROM sub_accounts WHERE id = ?', [subAccountId]);

        res.json({ success: true });
    } catch (error) {
        console.error('刪除子帳戶錯誤:', error);
        res.status(500).json({ error: '刪除子帳戶失敗' });
    }
});

// 子帳戶之間轉帳
app.post('/api/sub-accounts/transfer', async (req, res) => {
    const { userId, fromSubAccountId, toSubAccountId, amount } = req.body;

    try {
        const [fromAccount] = await db.query(
            'SELECT * FROM sub_accounts WHERE id = ? AND user_id = ?',
            [fromSubAccountId, userId]
        );

        const [toAccount] = await db.query(
            'SELECT * FROM sub_accounts WHERE id = ? AND user_id = ?',
            [toSubAccountId, userId]
        );

        if (fromAccount.length === 0 || toAccount.length === 0) {
            return res.status(404).json({ error: '子帳戶不存在' });
        }

        if (parseFloat(fromAccount[0].balance) < amount) {
            return res.status(400).json({ error: '子帳戶餘額不足' });
        }

        // 更新餘額
        const newFromBalance = parseFloat(fromAccount[0].balance) - amount;
        const newToBalance = parseFloat(toAccount[0].balance) + amount;

        await db.query('UPDATE sub_accounts SET balance = ? WHERE id = ?', [newFromBalance, fromSubAccountId]);
        await db.query('UPDATE sub_accounts SET balance = ? WHERE id = ?', [newToBalance, toSubAccountId]);

        // 記錄交易
        const transactionId = 'TXN' + Date.now() + Math.random();
        const time = new Date().toLocaleString('zh-TW');
        const note = `從「${fromAccount[0].name}」轉至「${toAccount[0].name}」NT$ ${amount.toLocaleString('zh-TW')}`;

        await db.query(
            'INSERT INTO transactions (id, user_id, type, amount, note, time, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)',
            [transactionId, userId, '內部轉帳', 0, note, time, Date.now()]
        );

        res.json({ success: true });
    } catch (error) {
        console.error('子帳戶轉帳錯誤:', error);
        res.status(500).json({ error: '子帳戶轉帳失敗' });
    }
});

// ===== 常用帳號相關 API =====

// 新增常用帳號
app.post('/api/favorite-accounts', async (req, res) => {
    const { userId, favoriteUserId } = req.body;

    try {
        await db.query(
            'INSERT IGNORE INTO favorite_accounts (user_id, favorite_user_id) VALUES (?, ?)',
            [userId, favoriteUserId]
        );

        res.json({ success: true });
    } catch (error) {
        console.error('新增常用帳號錯誤:', error);
        res.status(500).json({ error: '新增常用帳號失敗' });
    }
});

// 刪除常用帳號
app.delete('/api/favorite-accounts', async (req, res) => {
    const { userId, favoriteUserId } = req.query;

    try {
        await db.query(
            'DELETE FROM favorite_accounts WHERE user_id = ? AND favorite_user_id = ?',
            [userId, favoriteUserId]
        );

        res.json({ success: true });
    } catch (error) {
        console.error('刪除常用帳號錯誤:', error);
        res.status(500).json({ error: '刪除常用帳號失敗' });
    }
});

// 獲取常用帳號詳細資訊
app.get('/api/favorite-accounts/:userId', async (req, res) => {
    const { userId } = req.params;

    try {
        const [favorites] = await db.query(
            `SELECT u.id, u.real_name
             FROM favorite_accounts f
             JOIN users u ON f.favorite_user_id = u.id
             WHERE f.user_id = ?`,
            [userId]
        );

        res.json(favorites.map(f => ({
            id: f.id,
            accountNumber: f.id,
            realName: f.real_name
        })));
    } catch (error) {
        console.error('獲取常用帳號錯誤:', error);
        res.status(500).json({ error: '獲取常用帳號失敗' });
    }
});

// ===== 管理員相關 API =====

// 獲取所有用戶
app.get('/api/admin/users', async (req, res) => {
    try {
        const [users] = await db.query(
            'SELECT * FROM users WHERE role != ? ORDER BY created_at DESC',
            ['admin']
        );

        // 為每個用戶獲取子帳戶
        const usersWithSubAccounts = await Promise.all(users.map(async (user) => {
            const [subAccounts] = await db.query(
                'SELECT * FROM sub_accounts WHERE user_id = ?',
                [user.id]
            );

            const balance = subAccounts.reduce((sum, sub) => sum + parseFloat(sub.balance), 0);

            return {
                id: user.id,
                loginId: user.login_id,
                realName: user.real_name,
                balance: balance,
                role: user.role,
                status: user.status,
                createdAt: user.created_at
            };
        }));

        res.json(usersWithSubAccounts);
    } catch (error) {
        console.error('獲取所有用戶錯誤:', error);
        res.status(500).json({ error: '獲取所有用戶失敗' });
    }
});

// 更新用戶餘額
app.put('/api/admin/users/:userId/balance', async (req, res) => {
    const { userId } = req.params;
    const { newBalance } = req.body;

    try {
        // 獲取第一個子帳戶（主帳戶）
        const [subAccounts] = await db.query(
            'SELECT * FROM sub_accounts WHERE user_id = ? ORDER BY created_at LIMIT 1',
            [userId]
        );

        if (subAccounts.length === 0) {
            return res.status(404).json({ error: '找不到用戶帳戶' });
        }

        const subAccount = subAccounts[0];
        const diff = newBalance - parseFloat(subAccount.balance);

        // 更新餘額
        await db.query('UPDATE sub_accounts SET balance = ? WHERE id = ?', [newBalance, subAccount.id]);

        // 記錄交易
        const transactionId = 'TXN' + Date.now() + Math.random();
        const time = new Date().toLocaleString('zh-TW');
        await db.query(
            'INSERT INTO transactions (id, user_id, type, amount, note, time, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)',
            [transactionId, userId, '管理員調整', diff, '管理員調整餘額', time, Date.now()]
        );

        res.json({ success: true });
    } catch (error) {
        console.error('更新用戶餘額錯誤:', error);
        res.status(500).json({ error: '更新用戶餘額失敗' });
    }
});

// 切換用戶狀態
app.put('/api/admin/users/:userId/status', async (req, res) => {
    const { userId } = req.params;

    try {
        const [users] = await db.query('SELECT status FROM users WHERE id = ?', [userId]);
        if (users.length === 0) {
            return res.status(404).json({ error: '用戶不存在' });
        }

        const newStatus = users[0].status === 'active' ? 'frozen' : 'active';
        await db.query('UPDATE users SET status = ? WHERE id = ?', [newStatus, userId]);

        res.json({ success: true, status: newStatus });
    } catch (error) {
        console.error('切換用戶狀態錯誤:', error);
        res.status(500).json({ error: '切換用戶狀態失敗' });
    }
});

// 刪除用戶
app.delete('/api/admin/users/:userId', async (req, res) => {
    const { userId } = req.params;

    try {
        // 檢查不是管理員
        const [users] = await db.query('SELECT role FROM users WHERE id = ?', [userId]);
        if (users.length > 0 && users[0].role === 'admin') {
            return res.status(400).json({ error: '不能刪除管理員帳號' });
        }

        // 刪除用戶（外鍵會自動刪除相關資料）
        await db.query('DELETE FROM users WHERE id = ?', [userId]);

        res.json({ success: true });
    } catch (error) {
        console.error('刪除用戶錯誤:', error);
        res.status(500).json({ error: '刪除用戶失敗' });
    }
});

// 獲取所有交易記錄
app.get('/api/admin/transactions', async (req, res) => {
    try {
        const [transactions] = await db.query(
            'SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 100'
        );

        res.json(transactions.map(t => ({
            id: t.id,
            userId: t.user_id,
            type: t.type,
            amount: parseFloat(t.amount),
            note: t.note,
            time: t.time,
            timestamp: t.timestamp
        })));
    } catch (error) {
        console.error('獲取所有交易錯誤:', error);
        res.status(500).json({ error: '獲取所有交易失敗' });
    }
});

// 啟動伺服器
app.listen(PORT, () => {
    console.log(`✓ 伺服器運行於 http://localhost:${PORT}`);
});
