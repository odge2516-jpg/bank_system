<template>
  <div>
    <div class="header">
      <h1>🏦 帳戶交易管理系統</h1>
      <button class="logout-btn" @click="onLogoutClick">登出</button>
    </div>

    <div class="dashboard-grid">
      <AccountInfo 
        :current-user="currentUser" 
        @open-modal="onOpenModal"
      />
      
      <TransactionList 
        :transactions="transactions" 
      />
    </div>
  </div>
</template>

<script>
import AccountInfo from './AccountInfo.vue'
import TransactionList from './TransactionList.vue'

export default {
  name: 'Dashboard',
  components: {
    AccountInfo,
    TransactionList
  },
  emits: ['logout', 'open-modal'],
  props: {
    currentUser: {
      type: Object,
      required: true
    },
    transactions: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    onLogoutClick() {
      console.log('Dashboard: logout button clicked')
      this.$emit('logout')
    },
    onOpenModal(type) {
      console.log('Dashboard: open-modal event received', type)
      this.$emit('open-modal', type)
    }
  }
}
</script>

<style scoped>
.header {
  background: rgba(255, 255, 255, 0.95);
  padding: 20px 30px;
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  color: #1e3a8a;
  font-size: 28px;
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

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}
</style>
