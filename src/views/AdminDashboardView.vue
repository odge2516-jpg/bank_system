<template>
  <div class="admin-dashboard">
    <div class="admin-header">
      <h1>🔒 管理員控制台</h1>
      <div class="admin-info">
        <span>歡迎，{{ currentUser?.realName }}</span>
        <button class="logout-btn" @click="handleLogout">登出</button>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-label">總用戶數</div>
        <div class="stat-value">{{ allUsers.length }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">活躍用戶</div>
        <div class="stat-value">{{ activeUsers }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">凍結用戶</div>
        <div class="stat-value">{{ frozenUsers }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">總交易數</div>
        <div class="stat-value">{{ allTransactions.length }}</div>
      </div>
    </div>

    <div class="tabs">
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'users' }"
        @click="activeTab = 'users'"
      >
        用戶管理
      </button>
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'transactions' }"
        @click="activeTab = 'transactions'"
      >
        交易記錄
      </button>
    </div>

    <!-- 用戶管理頁籤 -->
    <div v-if="activeTab === 'users'" class="content-section">
      <div class="table-container">
        <table class="admin-table">
          <thead>
            <tr>
              <th>銀行帳號</th>
              <th>真實姓名</th>
              <th>登入帳號</th>
              <th>餘額</th>
              <th>狀態</th>
              <th>開戶時間</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in allUsers" :key="user.id">
              <td>{{ formatAccountNumber(user.id) }}</td>
              <td>{{ user.realName }}</td>
              <td>{{ user.loginId }}</td>
              <td>NT$ {{ formatAmount(user.balance) }}</td>
              <td>
                <span class="status-badge" :class="user.status">
                  {{ user.status === 'active' ? '正常' : '凍結' }}
                </span>
              </td>
              <td>{{ user.createdAt }}</td>
              <td>
                <div class="action-buttons">
                  <button class="btn-edit" @click="editUser(user)">編輯</button>
                  <button
                    class="btn-toggle"
                    :class="user.status"
                    @click="toggleStatus(user.id)"
                  >
                    {{ user.status === 'active' ? '凍結' : '解凍' }}
                  </button>
                  <button class="btn-delete" @click="confirmDelete(user)">刪除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 交易記錄頁籤 -->
    <div v-if="activeTab === 'transactions'" class="content-section">
      <div class="table-container">
        <table class="admin-table">
          <thead>
            <tr>
              <th>交易ID</th>
              <th>用戶帳號</th>
              <th>用戶姓名</th>
              <th>類型</th>
              <th>金額</th>
              <th>備註</th>
              <th>時間</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="transaction in allTransactions" :key="transaction.id">
              <td>{{ transaction.id.substring(0, 15) }}...</td>
              <td>{{ formatAccountNumber(transaction.userId) }}</td>
              <td>{{ getUserName(transaction.userId) }}</td>
              <td>{{ transaction.type }}</td>
              <td :class="transaction.amount >= 0 ? 'positive' : 'negative'">
                NT$ {{ formatAmount(Math.abs(transaction.amount)) }}
              </td>
              <td>{{ transaction.note || '-' }}</td>
              <td>{{ transaction.time }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 編輯用戶彈窗 -->
    <div v-if="showEditModal" class="modal" @click.self="closeEditModal">
      <div class="modal-content">
        <h3>編輯用戶資料</h3>
        <div v-if="editError" class="error-message">{{ editError }}</div>

        <div class="form-group">
          <label>真實姓名</label>
          <input v-model="editingUser.realName" type="text" disabled>
        </div>

        <div class="form-group">
          <label>銀行帳號</label>
          <input :value="formatAccountNumber(editingUser.id)" type="text" disabled>
        </div>

        <div class="form-group">
          <label>當前餘額</label>
          <input v-model.number="editingUser.balance" type="number" step="0.01">
        </div>

        <div class="modal-actions">
          <button @click="saveUserChanges" class="btn">儲存</button>
          <button @click="closeEditModal" class="btn btn-secondary">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'pinia'
import { useBankStore } from '../stores/bank'

export default {
  name: 'AdminDashboardView',
  data() {
    return {
      activeTab: 'users',
      showEditModal: false,
      editingUser: null,
      editError: '',
      allUsers: [],
      allTransactions: []
    }
  },
  computed: {
    ...mapState(useBankStore, ['currentUser']),
    activeUsers() {
      return this.allUsers.filter(u => u.status === 'active').length
    },
    frozenUsers() {
      return this.allUsers.filter(u => u.status === 'frozen').length
    }
  },
  async mounted() {
    // 檢查是否為管理員
    if (!this.currentUser || this.currentUser.role !== 'admin') {
      this.$router.replace('/')
      return
    }

    // 載入數據
    await this.loadData()
  },
  methods: {
    ...mapActions(useBankStore, ['logout', 'updateUserBalance', 'toggleUserStatus', 'deleteUser', 'getAllUsers', 'getAllTransactions']),

    async loadData() {
      this.allUsers = await this.getAllUsers()
      this.allTransactions = await this.getAllTransactions()
    },

    handleLogout() {
      this.logout()
      this.$nextTick(() => {
        this.$router.replace('/').catch(() => {})
      })
    },

    formatAccountNumber(accountNumber) {
      if (accountNumber && accountNumber.length === 12) {
        return accountNumber.slice(0, 4) + '-' + accountNumber.slice(4, 8) + '-' + accountNumber.slice(8)
      }
      return accountNumber
    },

    formatAmount(amount) {
      return amount.toLocaleString('zh-TW', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })
    },

    getUserName(userId) {
      const user = this.allUsers.find(u => u.id === userId)
      return user ? user.realName : '未知'
    },

    editUser(user) {
      this.editingUser = { ...user }
      this.showEditModal = true
      this.editError = ''
    },

    closeEditModal() {
      this.showEditModal = false
      this.editingUser = null
      this.editError = ''
    },

    async saveUserChanges() {
      if (this.editingUser.balance < 0) {
        this.editError = '餘額不能為負數'
        return
      }

      try {
        await this.updateUserBalance(this.editingUser.id, this.editingUser.balance)
        await this.loadData()
        this.closeEditModal()
      } catch (error) {
        this.editError = error.message
      }
    },

    async toggleStatus(userId) {
      if (confirm('確定要變更此用戶的帳戶狀態嗎？')) {
        try {
          await this.toggleUserStatus(userId)
          await this.loadData()
        } catch (error) {
          alert(error.message)
        }
      }
    },

    async confirmDelete(user) {
      if (confirm(`確定要刪除用戶「${user.realName}」嗎？此操作無法復原！`)) {
        try {
          await this.deleteUser(user.id)
          await this.loadData()
        } catch (error) {
          alert(error.message)
        }
      }
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.admin-header {
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
  color: white;
  padding: 30px;
  border-radius: 15px;
  margin-bottom: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.admin-header h1 {
  margin: 0;
  font-size: 28px;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.logout-btn {
  background: #ef4444;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.logout-btn:hover {
  background: #dc2626;
  transform: translateY(-2px);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 25px;
  border-radius: 15px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-label {
  color: #64748b;
  font-size: 14px;
  margin-bottom: 10px;
}

.stat-value {
  color: #1e3a8a;
  font-size: 36px;
  font-weight: 700;
}

.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.tab-btn {
  padding: 12px 24px;
  border: none;
  background: #e2e8f0;
  color: #475569;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s;
}

.tab-btn.active {
  background: #3b82f6;
  color: white;
}

.tab-btn:hover:not(.active) {
  background: #cbd5e1;
}

.content-section {
  background: white;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.table-container {
  overflow-x: auto;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
}

.admin-table th {
  background: #f1f5f9;
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #1e293b;
  border-bottom: 2px solid #e2e8f0;
}

.admin-table td {
  padding: 12px;
  border-bottom: 1px solid #e2e8f0;
  color: #475569;
}

.admin-table tr:hover {
  background: #f8fafc;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.active {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.frozen {
  background: #fee2e2;
  color: #991b1b;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-buttons button {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-edit {
  background: #3b82f6;
  color: white;
}

.btn-edit:hover {
  background: #2563eb;
}

.btn-toggle {
  background: #f59e0b;
  color: white;
}

.btn-toggle:hover {
  background: #d97706;
}

.btn-toggle.frozen {
  background: #10b981;
}

.btn-toggle.frozen:hover {
  background: #059669;
}

.btn-delete {
  background: #ef4444;
  color: white;
}

.btn-delete:hover {
  background: #dc2626;
}

.positive {
  color: #16a34a;
  font-weight: 600;
}

.negative {
  color: #dc2626;
  font-weight: 600;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 15px;
  max-width: 450px;
  width: 90%;
}

.modal-content h3 {
  color: #1e3a8a;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  color: #334155;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
}

.form-group input:disabled {
  background: #f1f5f9;
  color: #64748b;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.modal-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.btn {
  flex: 1;
  padding: 14px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn:hover {
  background: #5568d3;
}

.btn-secondary {
  background: #64748b;
}

.btn-secondary:hover {
  background: #475569;
}

.error-message {
  background: #fee2e2;
  color: #991b1b;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
  text-align: center;
}
</style>
