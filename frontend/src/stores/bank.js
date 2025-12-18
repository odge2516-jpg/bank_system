import { defineStore } from 'pinia'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

export const useBankStore = defineStore('bank', {
  state: () => ({
    currentUser: null,
    currentUserTransactions: [],
    favoriteAccountsDetails: [],
    authToken: null,
  }),

  getters: {
    // 計算 currentUser 的總餘額（所有子帳戶的總和）
    currentUserWithBalance: (state) => {
      if (!state.currentUser) return null

      const user = { ...state.currentUser }

      // 計算主帳戶餘額為所有子帳戶總和
      if (user.subAccounts && user.subAccounts.length > 0) {
        user.balance = user.subAccounts.reduce((sum, sub) => sum + sub.balance, 0)
      } else {
        user.balance = 0
      }

      return user
    },
  },

  actions: {
    async register(realName, loginId, password, initialDeposit) {
      try {
        const response = await fetch(`${API_URL}/register`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ realName, loginId, password, initialDeposit }),
        })

        const data = await response.json()

        if (!response.ok) {
          throw new Error(data.error || '註冊失敗')
        }

        return data.user
      } catch (error) {
        if (error.message === 'Failed to fetch') {
          throw new Error('無法連接伺服器，請確認後端服務是否已啟動')
        }
        throw new Error(error.message || '註冊失敗')
      }
    },

    async login(loginId, password) {
      try {
        const response = await fetch(`${API_URL}/login`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ loginId, password }),
        })

        const data = await response.json()

        if (!response.ok) {
          throw new Error(data.error || '登入失敗')
        }

        // Store JWT token
        this.authToken = data.token

        // 計算總餘額
        if (data.user.subAccounts && data.user.subAccounts.length > 0) {
          data.user.balance = data.user.subAccounts.reduce((sum, sub) => sum + sub.balance, 0)
        }

        this.currentUser = data.user
        // 登入後立即載入交易記錄和常用帳號
        await this.loadTransactions()
        await this.loadFavoriteAccounts()

        return data.user
      } catch (error) {
        if (error.message === 'Failed to fetch') {
          throw new Error('無法連接伺服器,請確認後端服務是否已啟動')
        }
        throw new Error(error.message || '登入失敗')
      }
    },

    logout() {
      this.currentUser = null
      this.currentUserTransactions = []
      this.favoriteAccountsDetails = []
      this.authToken = null
    },

    async loadUserData() {
      if (!this.currentUser) return

      try {
        const response = await fetch(`${API_URL}/user/${this.currentUser.id}`, {
          headers: this.authToken ? { Authorization: `Bearer ${this.authToken}` } : {},
        })
        const data = await response.json()

        if (response.ok) {
          // 計算總餘額
          if (data.subAccounts && data.subAccounts.length > 0) {
            data.balance = data.subAccounts.reduce((sum, sub) => sum + sub.balance, 0)
          }
          this.currentUser = data
        }
      } catch (error) {
        console.error('載入用戶資料失敗:', error)
      }
    },

    async loadTransactions() {
      if (!this.currentUser) return

      try {
        const response = await fetch(`${API_URL}/transactions/${this.currentUser.id}`, {
          headers: this.authToken ? { Authorization: `Bearer ${this.authToken}` } : {},
        })
        const data = await response.json()

        if (response.ok) {
          this.currentUserTransactions = data
        }
      } catch (error) {
        console.error('載入交易記錄失敗:', error)
      }
    },

    async loadFavoriteAccounts() {
      if (!this.currentUser) return

      try {
        const response = await fetch(`${API_URL}/favorite-accounts/${this.currentUser.id}`, {
          headers: this.authToken ? { Authorization: `Bearer ${this.authToken}` } : {},
        })
        const data = await response.json()

        if (response.ok) {
          this.favoriteAccountsDetails = data
        }
      } catch (error) {
        console.error('載入常用帳號失敗:', error)
      }
    },

    async deposit(amount, subAccountId) {
      if (!this.currentUser) return

      // 如果沒有指定子帳戶，使用第一個
      const targetSubAccountId = subAccountId || this.currentUser.subAccounts[0]?.id

      try {
        const response = await fetch(`${API_URL}/deposit`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...(this.authToken && { Authorization: `Bearer ${this.authToken}` }),
          },
          body: JSON.stringify({
            userId: this.currentUser.id,
            amount,
            subAccountId: targetSubAccountId,
          }),
        })

        const data = await response.json()

        if (!response.ok) {
          throw new Error(data.error || '存款失敗')
        }

        // 重新載入用戶資料和交易記錄
        await this.loadUserData()
        await this.loadTransactions()
      } catch (error) {
        throw new Error(error.message || '存款失敗')
      }
    },

    async withdraw(amount, subAccountId) {
      if (!this.currentUser) return

      // 如果沒有指定子帳戶，使用第一個
      const targetSubAccountId = subAccountId || this.currentUser.subAccounts[0]?.id

      try {
        const response = await fetch(`${API_URL}/withdraw`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...(this.authToken && { Authorization: `Bearer ${this.authToken}` }),
          },
          body: JSON.stringify({
            userId: this.currentUser.id,
            amount,
            subAccountId: targetSubAccountId,
          }),
        })

        const data = await response.json()

        if (!response.ok) {
          throw new Error(data.error || '提款失敗')
        }

        // 重新載入用戶資料和交易記錄
        await this.loadUserData()
        await this.loadTransactions()
      } catch (error) {
        throw new Error(error.message || '提款失敗')
      }
    },

    async transfer(recipientAccountNumber, amount, saveAsFavorite = false) {
      if (!this.currentUser) return

      try {
        const response = await fetch(`${API_URL}/transfer`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...(this.authToken && { Authorization: `Bearer ${this.authToken}` }),
          },
          body: JSON.stringify({
            userId: this.currentUser.id,
            recipientAccountNumber,
            amount,
            saveAsFavorite,
          }),
        })

        const data = await response.json()

        if (!response.ok) {
          throw new Error(data.error || '轉帳失敗')
        }

        // 重新載入用戶資料、交易記錄和常用帳號
        await this.loadUserData()
        await this.loadTransactions()
        await this.loadFavoriteAccounts()
      } catch (error) {
        throw new Error(error.message || '轉帳失敗')
      }
    },

    async removeFavoriteAccount(accountNumber) {
      if (!this.currentUser) return

      const cleanAccountNumber = accountNumber.replace(/[-\s]/g, '')

      try {
        const response = await fetch(
          `${API_URL}/favorite-accounts?userId=${this.currentUser.id}&favoriteUserId=${cleanAccountNumber}`,
          {
            method: 'DELETE',
            headers: this.authToken ? { Authorization: `Bearer ${this.authToken}` } : {},
          },
        )

        const data = await response.json()

        if (!response.ok) {
          throw new Error(data.error || '移除常用帳號失敗')
        }

        // 重新載入常用帳號
        await this.loadFavoriteAccounts()
      } catch (error) {
        throw new Error(error.message || '移除常用帳號失敗')
      }
    },

    maskAccountNumber(accountNumber) {
      // 遮蔽帳號中間部分 (例如: 1234****9012)
      if (accountNumber.length === 12) {
        return accountNumber.slice(0, 4) + '****' + accountNumber.slice(-4)
      }
      return accountNumber
    },

    formatAccountNumber(accountNumber) {
      // 格式化帳號顯示 (例如: 1234-5678-9012)
      if (accountNumber.length === 12) {
        return (
          accountNumber.slice(0, 4) + '-' + accountNumber.slice(4, 8) + '-' + accountNumber.slice(8)
        )
      }
      return accountNumber
    },

    // ===== 管理員專用功能 =====
    async getAllUsers() {
      try {
        const response = await fetch(`${API_URL}/admin/users`, {
          headers: this.authToken ? { Authorization: `Bearer ${this.authToken}` } : {},
        })
        const data = await response.json()

        if (response.ok) {
          return data
        }
        return []
      } catch (error) {
        console.error('獲取所有用戶失敗:', error)
        return []
      }
    },

    async updateUserBalance(userId, newBalance) {
      try {
        const response = await fetch(`${API_URL}/admin/users/${userId}/balance`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            ...(this.authToken && { Authorization: `Bearer ${this.authToken}` }),
          },
          body: JSON.stringify({ newBalance }),
        })

        const data = await response.json()

        if (!response.ok) {
          throw new Error(data.error || '更新餘額失敗')
        }
      } catch (error) {
        throw new Error(error.message || '更新餘額失敗')
      }
    },

    async toggleUserStatus(userId) {
      try {
        const response = await fetch(`${API_URL}/admin/users/${userId}/status`, {
          method: 'PUT',
          headers: this.authToken ? { Authorization: `Bearer ${this.authToken}` } : {},
        })

        const data = await response.json()

        if (!response.ok) {
          throw new Error(data.error || '切換狀態失敗')
        }

        return data.status
      } catch (error) {
        throw new Error(error.message || '切換狀態失敗')
      }
    },

    async deleteUser(userId) {
      try {
        const response = await fetch(`${API_URL}/admin/users/${userId}`, {
          method: 'DELETE',
          headers: this.authToken ? { Authorization: `Bearer ${this.authToken}` } : {},
        })

        const data = await response.json()

        if (!response.ok) {
          throw new Error(data.error || '刪除用戶失敗')
        }
      } catch (error) {
        throw new Error(error.message || '刪除用戶失敗')
      }
    },

    async getAllTransactions() {
      try {
        const response = await fetch(`${API_URL}/admin/transactions`, {
          headers: this.authToken ? { Authorization: `Bearer ${this.authToken}` } : {},
        })
        const data = await response.json()

        if (response.ok) {
          return data
        }
        return []
      } catch (error) {
        console.error('獲取所有交易失敗:', error)
        return []
      }
    },

    // ===== 子帳戶管理功能 =====
    async createSubAccount(name, color) {
      if (!this.currentUser) return

      try {
        const response = await fetch(`${API_URL}/sub-accounts`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...(this.authToken && { Authorization: `Bearer ${this.authToken}` }),
          },
          body: JSON.stringify({
            userId: this.currentUser.id,
            name,
            color,
          }),
        })

        const data = await response.json()

        if (!response.ok) {
          throw new Error(data.error || '創建子帳戶失敗')
        }

        // 重新載入用戶資料
        await this.loadUserData()
        return data.subAccount
      } catch (error) {
        throw new Error(error.message || '創建子帳戶失敗')
      }
    },

    async updateSubAccountName(subAccountId, newName) {
      if (!this.currentUser) return

      try {
        const response = await fetch(`${API_URL}/sub-accounts/${subAccountId}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            ...(this.authToken && { Authorization: `Bearer ${this.authToken}` }),
          },
          body: JSON.stringify({ name: newName }),
        })

        const data = await response.json()

        if (!response.ok) {
          throw new Error(data.error || '更新子帳戶失敗')
        }

        // 重新載入用戶資料
        await this.loadUserData()
      } catch (error) {
        throw new Error(error.message || '更新子帳戶失敗')
      }
    },

    async deleteSubAccount(subAccountId) {
      if (!this.currentUser) return

      try {
        const response = await fetch(
          `${API_URL}/sub-accounts/${subAccountId}?userId=${this.currentUser.id}`,
          {
            method: 'DELETE',
            headers: this.authToken ? { Authorization: `Bearer ${this.authToken}` } : {},
          },
        )

        const data = await response.json()

        if (!response.ok) {
          throw new Error(data.error || '刪除子帳戶失敗')
        }

        // 重新載入用戶資料
        await this.loadUserData()
      } catch (error) {
        throw new Error(error.message || '刪除子帳戶失敗')
      }
    },

    async transferBetweenSubAccounts(fromSubAccountId, toSubAccountId, amount) {
      if (!this.currentUser) return

      try {
        const response = await fetch(`${API_URL}/sub-accounts/transfer`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...(this.authToken && { Authorization: `Bearer ${this.authToken}` }),
          },
          body: JSON.stringify({
            userId: this.currentUser.id,
            fromSubAccountId,
            toSubAccountId,
            amount,
          }),
        })

        const data = await response.json()

        if (!response.ok) {
          throw new Error(data.error || '子帳戶轉帳失敗')
        }

        // 重新載入用戶資料和交易記錄
        await this.loadUserData()
        await this.loadTransactions()
      } catch (error) {
        throw new Error(error.message || '子帳戶轉帳失敗')
      }
    },
  },
})
