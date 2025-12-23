<template>
  <div class="sub-account-manager">
    <div class="manager-header">
      <h3>💼 子帳戶管理</h3>
      <button class="btn-add" @click="showAddModal = true">+ 新增子帳戶</button>
    </div>

    <!-- 子帳戶卡片列表 -->
    <div class="sub-accounts-grid">
      <div
        v-for="subAccount in subAccounts"
        :key="subAccount.id"
        class="sub-account-card"
        :style="{ borderLeftColor: subAccount.color }"
      >
        <div class="card-header">
          <div class="account-name-section">
            <div class="color-dot" :style="{ backgroundColor: subAccount.color }"></div>
            <h4>{{ subAccount.name }}</h4>
          </div>
          <div class="card-actions">
            <button class="btn-icon" @click="openEditModal(subAccount)" title="編輯名稱">✏️</button>
            <button
              v-if="subAccounts.length > 1"
              class="btn-icon"
              @click="confirmDelete(subAccount)"
              title="刪除"
            >
              🗑️
            </button>
          </div>
        </div>

        <div class="card-body">
          <div class="balance-section">
            <span class="balance-label">帳戶餘額</span>
            <span class="balance-amount">NT$ {{ formatAmount(subAccount.balance) }}</span>
          </div>

          <div class="action-buttons">
            <button class="btn-action btn-transfer" @click="openTransferModal(subAccount)">
              轉入/轉出
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增子帳戶彈窗 -->
    <div v-if="showAddModal" class="modal" @click.self="closeAddModal">
      <div class="modal-content">
        <h3>新增子帳戶</h3>
        <div v-if="modalError" class="error-message">{{ modalError }}</div>

        <div class="form-group">
          <label>帳戶名稱</label>
          <input
            v-model="newAccountName"
            type="text"
            placeholder="例如：生活費、娛樂、繳稅等"
            maxlength="20"
          />
        </div>

        <div class="form-group">
          <label>帳戶顏色</label>
          <div class="color-picker">
            <div
              v-for="color in availableColors"
              :key="color"
              class="color-option"
              :class="{ selected: newAccountColor === color }"
              :style="{ backgroundColor: color }"
              @click="newAccountColor = color"
            ></div>
          </div>
        </div>

        <div class="modal-actions">
          <button @click="createAccount" class="btn">確認新增</button>
          <button @click="closeAddModal" class="btn btn-secondary">取消</button>
        </div>
      </div>
    </div>

    <!-- 編輯子帳戶彈窗 -->
    <div v-if="showEditModal" class="modal" @click.self="closeEditModal">
      <div class="modal-content">
        <h3>編輯子帳戶</h3>
        <div v-if="modalError" class="error-message">{{ modalError }}</div>

        <div class="form-group">
          <label>帳戶名稱</label>
          <input
            v-model="editingAccount.name"
            type="text"
            placeholder="請輸入帳戶名稱"
            maxlength="20"
          />
        </div>

        <div class="modal-actions">
          <button @click="saveAccountName" class="btn">儲存</button>
          <button @click="closeEditModal" class="btn btn-secondary">取消</button>
        </div>
      </div>
    </div>

    <!-- 子帳戶轉帳彈窗 -->
    <div v-if="showTransferModal" class="modal" @click.self="closeTransferModal">
      <div class="modal-content">
        <h3>子帳戶轉帳</h3>
        <div v-if="modalError" class="error-message">{{ modalError }}</div>

        <div class="form-group">
          <label>從</label>
          <select v-model="transferFrom">
            <option v-for="sub in subAccounts" :key="sub.id" :value="sub.id">
              {{ sub.name }} (餘額: NT$ {{ formatAmount(sub.balance) }})
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>轉到</label>
          <select v-model="transferTo">
            <option
              v-for="sub in subAccounts"
              :key="sub.id"
              :value="sub.id"
              :disabled="sub.id === transferFrom"
            >
              {{ sub.name }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>金額</label>
          <input
            v-model.number="transferAmount"
            type="number"
            min="1"
            step="1"
            placeholder="請輸入轉帳金額"
          />
        </div>

        <div class="modal-actions">
          <button @click="executeTransfer" class="btn">確認轉帳</button>
          <button @click="closeTransferModal" class="btn btn-secondary">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'pinia'
import { useBankStore } from '../stores/bank'

export default {
  name: 'SubAccountManager',
  props: {
    subAccounts: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      showAddModal: false,
      showEditModal: false,
      showTransferModal: false,
      newAccountName: '',
      newAccountColor: '#3b82f6',
      editingAccount: null,
      transferFrom: null,
      transferTo: null,
      transferAmount: null,
      modalError: '',
      availableColors: [
        '#3b82f6',
        '#10b981',
        '#f59e0b',
        '#ef4444',
        '#8b5cf6',
        '#ec4899',
        '#06b6d4',
        '#84cc16',
      ],
    }
  },
  methods: {
    ...mapActions(useBankStore, [
      'createSubAccount',
      'updateSubAccountName',
      'deleteSubAccount',
      'transferBetweenSubAccounts',
    ]),

    formatAmount(amount) {
      return Math.floor(amount).toLocaleString('zh-TW')
    },

    closeAddModal() {
      this.showAddModal = false
      this.newAccountName = ''
      this.newAccountColor = '#3b82f6'
      this.modalError = ''
    },

    createAccount() {
      if (!this.newAccountName.trim()) {
        this.modalError = '請輸入帳戶名稱'
        return
      }

      this.createSubAccount(this.newAccountName, this.newAccountColor)
      this.closeAddModal()
    },

    openEditModal(subAccount) {
      this.editingAccount = { ...subAccount }
      this.showEditModal = true
      this.modalError = ''
    },

    closeEditModal() {
      this.showEditModal = false
      this.editingAccount = null
      this.modalError = ''
    },

    saveAccountName() {
      if (!this.editingAccount.name.trim()) {
        this.modalError = '請輸入帳戶名稱'
        return
      }

      this.updateSubAccountName(this.editingAccount.id, this.editingAccount.name)
      this.closeEditModal()
    },

    confirmDelete(subAccount) {
      if (confirm(`確定要刪除子帳戶「${subAccount.name}」嗎？`)) {
        try {
          this.deleteSubAccount(subAccount.id)
        } catch (error) {
          alert(error.message)
        }
      }
    },

    openTransferModal(subAccount) {
      this.transferFrom = subAccount.id
      this.transferTo = this.subAccounts.find((s) => s.id !== subAccount.id)?.id || null
      this.transferAmount = null
      this.showTransferModal = true
      this.modalError = ''
    },

    closeTransferModal() {
      this.showTransferModal = false
      this.transferFrom = null
      this.transferTo = null
      this.transferAmount = null
      this.modalError = ''
    },

    executeTransfer() {
      if (!this.transferFrom || !this.transferTo) {
        this.modalError = '請選擇轉出和轉入帳戶'
        return
      }

      if (this.transferFrom === this.transferTo) {
        this.modalError = '不能轉給同一個帳戶'
        return
      }

      if (!this.transferAmount || this.transferAmount <= 0) {
        this.modalError = '請輸入有效金額'
        return
      }

      try {
        this.transferBetweenSubAccounts(this.transferFrom, this.transferTo, this.transferAmount)
        this.closeTransferModal()
      } catch (error) {
        this.modalError = error.message
      }
    },
  },
}
</script>

<style scoped>
.sub-account-manager {
  margin-top: 30px;
  background: white;
  padding: 30px;
  border-radius: 15px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.manager-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.manager-header h3 {
  color: #1e3a8a;
  font-size: 24px;
  margin: 0;
}

.btn-add {
  background: #10b981;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-add:hover {
  background: #059669;
  transform: translateY(-2px);
}

.sub-accounts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.sub-account-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  border-left: 4px solid #3b82f6;
  transition: all 0.3s;
}

.sub-account-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.account-name-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.card-header h4 {
  margin: 0;
  color: #1e293b;
  font-size: 18px;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.btn-icon {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 16px;
  transition: transform 0.2s;
}

.btn-icon:hover {
  transform: scale(1.2);
}

.card-body {
  margin-top: 15px;
}

.balance-section {
  background: white;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.balance-label {
  display: block;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 5px;
}

.balance-amount {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #1e3a8a;
  font-family: 'Courier New', monospace;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.btn-action {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-transfer {
  background: #3b82f6;
  color: white;
}

.btn-transfer:hover {
  background: #2563eb;
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

.form-group input,
.form-group select {
  width: 100%;
  padding: 12px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #667eea;
}

.color-picker {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.color-option {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  cursor: pointer;
  border: 3px solid transparent;
  transition: all 0.2s;
}

.color-option:hover {
  transform: scale(1.1);
}

.color-option.selected {
  border-color: #1e3a8a;
  transform: scale(1.15);
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
