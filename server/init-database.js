const mysql = require('mysql2/promise');
const fs = require('fs');
const path = require('path');
require('dotenv').config();

async function initializeDatabase() {
    let connection;

    try {
        // 首先連接到 MySQL（不指定資料庫）
        connection = await mysql.createConnection({
            host: process.env.DB_HOST,
            port: process.env.DB_PORT,
            user: process.env.DB_USER,
            password: process.env.DB_PASSWORD,
            multipleStatements: true
        });

        console.log('已連接到 MySQL 伺服器');

        // 讀取並執行 SQL schema
        const schemaPath = path.join(__dirname, 'schema.sql');
        const schema = fs.readFileSync(schemaPath, 'utf8');

        console.log('開始執行資料庫初始化...');
        await connection.query(schema);
        console.log('✓ 資料庫初始化完成！');
        console.log('✓ 已創建資料庫: bank_system');
        console.log('✓ 已創建表: users, sub_accounts, transactions, favorite_accounts');
        console.log('✓ 已插入預設管理員帳號: admin / admin123');

    } catch (error) {
        console.error('資料庫初始化失敗:', error.message);
        process.exit(1);
    } finally {
        if (connection) {
            await connection.end();
            console.log('\n資料庫連接已關閉');
        }
    }
}

initializeDatabase();
