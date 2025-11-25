銀行帳戶交易管理系統

基於 Vue 3 + Express.js + MySQL 的線上銀行系統。

功能特色:
- 用戶功能: 註冊/登入、存提轉帳、交易記錄、常用帳號、子帳戶管理。
- 管理員功能: 用戶管理、餘額調整、凍結帳戶、查看交易。

技術棧:
- 前端: Vue 3, Pinia, Vue Router, Chart.js, Vite
- 後端: Node.js, Express.js, MySQL

安裝與執行:
1. 前端安裝: npm install
2. 後端安裝: cd server && npm install
3. 資料庫設定: 在 server/ 建立 .env (參考範例), 執行 node init-database.js 初始化。
4. 啟動後端: cd server && npm start (Port 3000)
5. 啟動前端: npm run dev (Port 5173)

預設帳號:
- 管理員: admin / admin123

授權: MIT License
作者: Brian Wen
