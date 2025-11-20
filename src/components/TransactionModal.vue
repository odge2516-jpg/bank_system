<template>
  <div v-if="show" class="modal" @click.self="$emit('close')">
    <div class="modal-content">
      <h3>{{ modalTitle }}</h3>
      <div v-if="error" class="error-message">{{ error }}</div>

      <div v-if="type === 'transfer'">
        <!-- 常用帳號列表 -->
        <div v-if="favoriteAccounts && favoriteAccounts.length > 0" class="favorite-accounts">
          <label>常用帳號</label>
          <div class="favorite-list">
            <div
              v-for="account in favoriteAccounts"
              :key="account.id"
              class="favorite-item"
              :class="{ active: localRecipientAccount === account.accountNumber }"
              @click="selectFavoriteAccount(account.accountNumber)"
            >
              <div class="favorite-name">{{ account.realName }}</div>
              <div class="favorite-account">{{ formatAccountNumber(account.accountNumber) }}</div>
              <button
                class="remove-favorite"
                @click.stop="$emit('remove-favorite', account.accountNumber)"
                title="移除常用帳號"
              >×</button>
            </div>
          </div>
        </div>

        <!-- 手動輸入帳號 -->
        <div class="form-group">
          <label>收款帳號</label>
          <input
            v-model="localRecipientAccount"
            type="text"
            placeholder="請輸入12位數帳號（例如：1234-5678-9012）"
            maxlength="14"
            @input="formatAccountInput"
          >
          <div class="input-hint">請輸入對方的銀行帳號</div>
        </div>

        <!-- 加入常用帳號選項 -->
        <div class="form-group">
          <label class="checkbox-label">
            <input type="checkbox" v-model="saveAsFavorite">
            <span>加入常用帳號</span>
          </label>
        </div>
      </div>

      <!-- 子帳戶選擇（存款/提款時） -->
      <div v-if="(type === 'deposit' || type === 'withdraw') && subAccounts.length > 0" class="form-group">
        <label>選擇子帳戶</label>
        <select v-model="selectedSubAccount">
          <option
            v-for="sub in subAccounts"
            :key="sub.id"
            :value="sub.id"
          >
            {{ sub.name }} (餘額: NT$ {{ formatSubAccountBalance(sub.balance) }})
          </option>
        </select>
      </div>

      <div class="form-group">
        <label>金額</label>
        <input v-model="localAmount" type="number" min="0.01" step="0.01" placeholder="請輸入金額">
      </div>

      <div class="modal-actions">
        <button @click="handleSubmit" class="btn">確認</button>
        <button @click="$emit('close')" class="btn btn-secondary">取消</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TransactionModal',
  props: {
    show: Boolean,
    type: String,
    favoriteAccounts: {
      type: Array,
      default: () => []
    },
    subAccounts: {
      type: Array,
      default: () => []
    },
    error: String
  },
  emits: ['close', 'submit', 'remove-favorite'],
  data() {
    return {
      localAmount: '',
      localRecipientAccount: '',
      saveAsFavorite: false,
      selectedSubAccount: null
    }
  },
  computed: {
    modalTitle() {
      const titles = {
        deposit: '存款',
        withdraw: '提款',
        transfer: '轉帳'
      }
      return titles[this.type] || ''
    }
  },
  watch: {
    show(val) {
      if (val) {
        this.localAmount = ''
        this.localRecipientAccount = ''
        this.saveAsFavorite = false
        // 預設選擇第一個子帳戶
        this.selectedSubAccount = this.subAccounts.length > 0 ? this.subAccounts[0].id : null
      }
    }
  },
  methods: {
    formatAccountNumber(accountNumber) {
      // 格式化為 1234-5678-9012
      if (accountNumber && accountNumber.length === 12) {
        return accountNumber.slice(0, 4) + '-' + accountNumber.slice(4, 8) + '-' + accountNumber.slice(8)
      }
      return accountNumber
    },
    formatAccountInput(event) {
      // 自動格式化輸入的帳號（添加連字符）
      let value = event.target.value.replace(/[^\d]/g, '') // 移除非數字字符
      if (value.length > 4 && value.length <= 8) {
        value = value.slice(0, 4) + '-' + value.slice(4)
      } else if (value.length > 8) {
        value = value.slice(0, 4) + '-' + value.slice(4, 8) + '-' + value.slice(8, 12)
      }
      this.localRecipientAccount = value
    },
    selectFavoriteAccount(accountNumber) {
      this.localRecipientAccount = this.formatAccountNumber(accountNumber)
    },
    handleSubmit() {
      this.$emit('submit', {
        amount: this.localAmount,
        recipientAccount: this.localRecipientAccount,
        saveAsFavorite: this.saveAsFavorite,
        subAccountId: this.selectedSubAccount
      })
    },
    formatSubAccountBalance(balance) {
      return balance.toLocaleString('zh-TW', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })
    }
  }
}
</script>

<style scoped>
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

.form-group input, select {
  width: 100%;
  padding: 12px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
  background: white;
}

.form-group input:focus, select:focus {
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

.input-hint {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.checkbox-label input[type="checkbox"] {
  width: auto;
  cursor: pointer;
}

.checkbox-label span {
  color: #334155;
}

.favorite-accounts {
  margin-bottom: 20px;
}

.favorite-accounts label {
  display: block;
  color: #334155;
  margin-bottom: 8px;
  font-weight: 500;
}

.favorite-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
  padding: 4px;
}

.favorite-item {
  position: relative;
  background: #f1f5f9;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.favorite-item:hover {
  background: #e2e8f0;
  transform: translateX(4px);
}

.favorite-item.active {
  background: #dbeafe;
  border-color: #3b82f6;
}

.favorite-name {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.favorite-account {
  font-size: 14px;
  color: #64748b;
  font-family: 'Courier New', monospace;
}

.remove-favorite {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #ef4444;
  color: white;
  border: none;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.remove-favorite:hover {
  background: #dc2626;
  transform: scale(1.1);
}
</style>
