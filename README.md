# 🏦 銀行帳戶交易管理系統

一個功能完整的線上銀行系統，使用 Vue 3 + Express.js + MySQL 開發。

## ✨ 功能特色

### 用戶功能
- ✅ 用戶註冊與登入（真實姓名 + 登入帳號 + 系統自動生成12位數銀行帳號）
- 💰 存款、提款、轉帳功能
- 📊 交易記錄可視化（使用 Chart.js）
- 🔖 常用帳號管理（加入/移除常用轉帳對象）
- 💼 子帳戶管理（類似郵局/台新銀行的子帳戶功能）
  - 創建多個子帳戶（生活費、娛樂、繳稅等）
  - 子帳戶之間轉帳
  - 主帳戶餘額 = 所有子帳戶總和
- 🎨 自訂子帳戶顏色與名稱

### 管理員功能
- 👥 查看所有用戶資料
- 💵 調整用戶餘額
- 🔒 凍結/解凍用戶帳戶
- 🗑️ 刪除用戶
- 📈 查看所有交易記錄

### 安全性
- 🔐 角色權限控制（一般用戶 / 管理員）
- 🚫 帳戶狀態管理（正常 / 凍結）
- 🔒 資料隱私保護（轉帳時遮蔽帳號中間位數）

## 🛠️ 技術棧

### 前端
- Vue 3 (Composition API)
- Pinia (狀態管理)
- Vue Router (路由管理)
- Chart.js / vue-chartjs (數據可視化)
- Vite (建置工具)

### 後端
- Node.js
- Express.js
- MySQL (資料庫)
- mysql2 (MySQL 驅動)
- CORS (跨域處理)

## 📦 專案結構

```
bank-system/
├── src/                      # 前端源碼
│   ├── components/          # Vue 組件
│   ├── views/              # 頁面視圖
│   ├── stores/             # Pinia stores
│   └── router/             # 路由設定
├── server/                  # 後端源碼
│   ├── config/             # 資料庫設定
│   ├── index.js            # Express 伺服器
│   ├── schema.sql          # 資料庫結構
│   └── init-database.js    # 資料庫初始化腳本
└── public/                  # 靜態資源
```

## 🚀 安裝與設定

### 環境需求
- Node.js >= 16
- MySQL >= 5.7
- npm 或 yarn

### 1. 克隆專案
```bash
git clone https://github.com/odge2516-jpg/bank_system.git
cd bank_system
```

### 2. 安裝前端依賴
```bash
npm install
```

### 3. 安裝後端依賴
```bash
cd server
npm install
```

### 4. 設定 MySQL 資料庫

在 `server/` 目錄下創建 `.env` 檔案：

```env
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=your_password
DB_NAME=bank_system
SERVER_PORT=3000
```

### 5. 初始化資料庫
```bash
cd server
node init-database.js
```

這將會：
- 創建 `bank_system` 資料庫
- 創建所有必要的資料表（users, sub_accounts, transactions, favorite_accounts）
- 插入預設管理員帳號（帳號: admin, 密碼: admin123）

### 6. 啟動後端伺服器
```bash
cd server
npm start
```
後端伺服器將運行於 `http://localhost:3000`

### 7. 啟動前端開發伺服器
在專案根目錄：
```bash
npm run dev
```
前端將運行於 `http://localhost:5173` (或其他可用端口)

## 📝 使用說明

### 管理員登入
- 帳號：`admin`
- 密碼：`admin123`

### 一般用戶註冊
1. 點擊「切換到註冊模式」
2. 填寫：
   - 真實姓名
   - 登入帳號（自訂）
   - 密碼
   - 初始存款金額
3. 系統會自動生成12位數銀行帳號
4. 註冊成功後可使用登入帳號登入

### 轉帳功能
1. 輸入對方的12位數銀行帳號（可使用連字符格式：1234-5678-9012）
2. 輸入金額
3. 可選擇加入常用帳號
4. 確認轉帳

### 子帳戶管理
1. 在儀表板點擊「新增子帳戶」
2. 輸入帳戶名稱（如：生活費、娛樂）
3. 選擇顏色標籤
4. 可在子帳戶之間轉帳分配資金

## 📊 資料庫結構

### users 表
- 儲存用戶基本資訊、登入憑證、角色權限

### sub_accounts 表
- 儲存用戶的子帳戶資訊與餘額

### transactions 表
- 記錄所有交易歷史

### favorite_accounts 表
- 儲存用戶的常用帳號清單

## 🔧 開發指令

### 前端
```bash
npm run dev      # 開發模式
npm run build    # 生產環境建置
npm run lint     # 程式碼檢查
```

### 後端
```bash
npm start        # 啟動伺服器
```

## 🎯 待改進功能
- [ ] 交易密碼驗證
- [ ] 交易限額設定
- [ ] 郵件通知
- [ ] 交易報表匯出
- [ ] 多語言支援
- [ ] 密碼加密（目前為明文儲存，僅供學習用途）

## 📄 授權
MIT License

## 👤 作者
Brian Wen

## 🙏 致謝
此專案為學習與展示用途，不建議直接用於生產環境。
