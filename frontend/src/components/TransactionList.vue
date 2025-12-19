<template>
  <div class="card">
    <h3>交易記錄</h3>
    <div v-if="transactions.length > 0" class="transaction-list">
      <div v-for="transaction in transactions" :key="transaction.id" class="transaction-item">
        <div class="transaction-info">
          <div class="transaction-type">{{ transaction.type }}</div>
          <div class="transaction-time">{{ transaction.time }}</div>
          <div v-if="transaction.note" style="font-size: 12px; color: #64748b; margin-top: 4px">
            {{ transaction.note }}
          </div>
        </div>
        <div
          :class="[
            'transaction-amount',
            transaction.amount > 0 ? 'amount-positive' : 'amount-negative',
          ]"
        >
          {{ transaction.amount > 0 ? '+' : '' }}NT$
          {{ formatAmount(Math.abs(transaction.amount)) }}
        </div>
      </div>
    </div>
    <div v-else class="empty-state">
      <p>尚無交易記錄</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TransactionList',
  props: {
    transactions: {
      type: Array,
      default: () => [],
    },
  },
  methods: {
    formatAmount(amount) {
      return amount.toLocaleString('zh-TW', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
      })
    },
  },
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

.transaction-list {
  max-height: 400px;
  overflow-y: auto;
}

.transaction-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #e2e8f0;
  transition: background 0.2s;
}

.transaction-item:hover {
  background: #f8fafc;
}

.transaction-item:last-child {
  border-bottom: none;
}

.transaction-info {
  flex: 1;
}

.transaction-type {
  font-weight: 600;
  color: #334155;
  margin-bottom: 4px;
}

.transaction-time {
  font-size: 12px;
  color: #94a3b8;
}

.transaction-amount {
  font-size: 18px;
  font-weight: 700;
  font-family: 'Courier New', monospace;
}

.amount-positive {
  color: #16a34a;
}

.amount-negative {
  color: #dc2626;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #94a3b8;
}
</style>
