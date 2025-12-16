銀行帳戶交易管理系統 (Bank System)

這是一個結合現代化 Vue 3 前端與穩健 Java Spring Boot 後端的銀行模擬平台。

[功能特色]

- 用戶功能: 註冊/登入、存款、提款、轉帳（模擬）、交易記錄查詢。
- 子帳戶管理: 可創建多個子帳戶以進行資產分配。
- 管理員後台: 用戶管理、系統監控與交易審查。
- 安全性: 採用 JWT 進行身份驗證。

[技術棧]

- 前端: Vue 3, Vite, Pinia, Vue Router, Chart.js
- 後端: Java 17, Spring Boot 3.2, Spring Data JPA
- 資料庫: MySQL

[快速開始]

1. 資料庫設定
   請建立一個名為 bank_system 的 MySQL 資料庫，並確認 backend-java/src/main/resources/application.properties 中的連線設定正確。

2. 啟動後端
   進入 backend-java 資料夾，執行 ./mvnw spring-boot:run
   伺服器預設運行於 http://localhost:8080

3. 啟動前端
   在根目錄執行 npm install 後，執行 npm run dev
   客戶端預設運行於 http://localhost:5173

[預設帳號]

- 管理員: admin / admin123
- 一般用戶: 請自行註冊新帳號。

授權: MIT License
