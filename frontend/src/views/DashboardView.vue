<template>
  <div class="dashboard-view">
    <div v-if="!currentUser" class="loading">載入中...</div>
    <template v-else>
      <Dashboard
        :current-user="currentUser"
        :transactions="currentUserTransactions"
        @logout="handleLogout"
        @open-modal="openModal"
      />

      <SubAccountManager v-if="currentUser.subAccounts" :sub-accounts="currentUser.subAccounts" />

      <TransactionCharts :transactions="currentUserTransactions" />

      <TransactionModal
        :show="showModal"
        :type="modalType"
        :favorite-accounts="favoriteAccountsDetails"
        :sub-accounts="currentUser.subAccounts || []"
        :error="modalError"
        @close="closeModal"
        @submit="handleTransaction"
        @remove-favorite="removeFavorite"
      />
    </template>
  </div>
</template>

<script>
import { mapState, mapActions } from 'pinia'
import { useBankStore } from '../stores/bank'
import Dashboard from '../components/Dashboard.vue'
import TransactionModal from '../components/TransactionModal.vue'
import TransactionCharts from '../components/TransactionCharts.vue'
import SubAccountManager from '../components/SubAccountManager.vue'

export default {
  name: 'DashboardView',
  components: {
    Dashboard,
    TransactionModal,
    TransactionCharts,
    SubAccountManager,
  },
  data() {
    return {
      showModal: false,
      modalType: '',
      modalError: '',
    }
  },
  computed: {
    ...mapState(useBankStore, [
      'currentUser',
      'currentUserTransactions',
      'favoriteAccountsDetails',
    ]),
  },
  beforeMount() {
    // 如果未登入，重定向到登入頁
    if (!this.currentUser) {
      this.$router.replace('/')
    }
  },
  methods: {
    ...mapActions(useBankStore, ['deposit', 'withdraw', 'transfer', 'removeFavoriteAccount']),

    handleLogout() {
      const store = useBankStore()
      store.logout()
      // 使用 nextTick 確保 store 狀態完全更新後再導航
      this.$nextTick(() => {
        this.$router.replace('/').catch(() => {})
      })
    },

    openModal(type) {
      this.modalType = type
      this.showModal = true
      this.modalError = ''
    },

    closeModal() {
      this.showModal = false
      this.modalType = ''
      this.modalError = ''
    },

    handleTransaction({ amount, recipientAccount, saveAsFavorite, subAccountId }) {
      const numAmount = Number(amount)

      if (isNaN(numAmount) || numAmount <= 0) {
        this.modalError = '請輸入有效金額'
        return
      }

      try {
        if (this.modalType === 'deposit') {
          this.deposit(numAmount, subAccountId)
        } else if (this.modalType === 'withdraw') {
          this.withdraw(numAmount, subAccountId)
        } else if (this.modalType === 'transfer') {
          if (!recipientAccount) {
            this.modalError = '請輸入收款帳號'
            return
          }
          this.transfer(recipientAccount, numAmount, saveAsFavorite)
        }
        this.closeModal()
      } catch (error) {
        this.modalError = error.message
      }
    },

    removeFavorite(accountNumber) {
      this.removeFavoriteAccount(accountNumber)
    },
  },
}
</script>

<style scoped>
.dashboard-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.loading {
  text-align: center;
  padding: 50px;
  font-size: 18px;
  color: #64748b;
}
</style>
