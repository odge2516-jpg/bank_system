<template>
  <div class="card">
    <h3>帳戶資訊</h3>
    <div class="balance-display">
      <div class="balance-label">當前餘額</div>
      <div class="balance-amount">NT$ {{ formatAmount(userBalance) }}</div>
    </div>
    <div class="account-info">
      <p><strong>戶名：</strong>{{ currentUser.realName }}</p>
      <p><strong>銀行帳號：</strong>{{ formatAccountNumber(currentUser.id) }}</p>
      <p><strong>開戶時間：</strong>{{ currentUser.createdAt }}</p>
    </div>
    <div class="transaction-actions">
      <button class="action-btn deposit" @click="$emit('open-modal', 'deposit')">存款</button>
      <button class="action-btn withdraw" @click="$emit('open-modal', 'withdraw')">提款</button>
      <button class="action-btn transfer" @click="$emit('open-modal', 'transfer')">轉帳</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AccountInfo',
  props: {
    currentUser: {
      type: Object,
      required: true
    }
  },
  emits: ['open-modal'],
  computed: {
    userBalance() {
      if (!this.currentUser) return 0
      // 如果有 balance 就用它，否則計算子帳戶總和
      if (this.currentUser.balance !== undefined) {
        return this.currentUser.balance
      }
      if (this.currentUser.subAccounts && this.currentUser.subAccounts.length > 0) {
        return this.currentUser.subAccounts.reduce((sum, sub) => sum + sub.balance, 0)
      }
      return 0
    }
  },
  methods: {
    formatAmount(amount) {
      if (amount === undefined || amount === null) return '0.00'
      return amount.toLocaleString('zh-TW', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })
    },
    formatAccountNumber(accountNumber) {
      // 格式化銀行帳號為 1234-5678-9012
      if (accountNumber && accountNumber.length === 12) {
        return accountNumber.slice(0, 4) + '-' + accountNumber.slice(4, 8) + '-' + accountNumber.slice(8)
      }
      return accountNumber
    }
  }
}
</script>

<style scoped>
.card {
  background: white;
  padding: 30px;
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card h3 {
  color: #1e3a8a;
  margin-bottom: 20px;
  font-size: 20px;
}

.balance-display {
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
  padding: 30px;
  border-radius: 15px;
  color: white;
  text-align: center;
  margin-bottom: 20px;
}

.balance-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 10px;
}

.balance-amount {
  font-size: 42px;
  font-weight: 700;
  font-family: 'Courier New', monospace;
}

.account-info {
  background: #f8fafc;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.account-info p {
  color: #475569;
  margin: 8px 0;
}

.transaction-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.action-btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn.deposit {
  background: #16a34a;
  color: white;
}

.action-btn.deposit:hover {
  background: #15803d;
}

.action-btn.withdraw {
  background: #dc2626;
  color: white;
}

.action-btn.withdraw:hover {
  background: #b91c1c;
}

.action-btn.transfer {
  background: #2563eb;
  color: white;
}

.action-btn.transfer:hover {
  background: #1d4ed8;
}
</style>
