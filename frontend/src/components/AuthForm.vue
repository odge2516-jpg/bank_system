<template>
  <div class="auth-container">
    <h2>{{ isLoginMode ? '帳戶登入' : '帳戶註冊' }}</h2>

    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
    <div v-if="successMessage" class="success-message">{{ successMessage }}</div>

    <div v-if="!isLoginMode" class="form-group">
      <label>真實姓名</label>
      <input v-model="realName" type="text" placeholder="請輸入真實姓名" />
    </div>

    <div class="form-group">
      <label>{{ isLoginMode ? '登入帳號' : '設定登入帳號' }}</label>
      <input
        v-model="loginId"
        type="text"
        :placeholder="isLoginMode ? '請輸入登入帳號' : '請設定登入帳號（英文或數字）'"
      />
      <div v-if="!isLoginMode" class="input-hint">此帳號用於登入系統，請妥善保管</div>
    </div>

    <div class="form-group">
      <label>密碼</label>
      <input v-model="password" type="password" placeholder="請輸入密碼" />
    </div>

    <div v-if="!isLoginMode" class="form-group">
      <label>初始存款金額</label>
      <input
        v-model="initialDeposit"
        type="number"
        min="0"
        step="100"
        placeholder="請輸入初始金額"
      />
    </div>

    <button @click="handleSubmit" class="btn">
      {{ isLoginMode ? '登入' : '註冊' }}
    </button>
    <button @click="toggleMode" class="btn btn-secondary">
      {{ isLoginMode ? '還沒有帳號？立即註冊' : '已有帳號？返回登入' }}
    </button>
  </div>
</template>

<script>
export default {
  name: 'AuthForm',
  emits: ['login', 'register'],
  data() {
    return {
      isLoginMode: true,
      realName: '',
      loginId: '',
      password: '',
      initialDeposit: 1000,
      errorMessage: '',
      successMessage: '',
    }
  },
  methods: {
    toggleMode() {
      this.isLoginMode = !this.isLoginMode
      this.errorMessage = ''
      this.successMessage = ''
      this.realName = ''
      this.loginId = ''
      this.password = ''
      this.initialDeposit = 1000
    },
    handleSubmit() {
      this.errorMessage = ''

      if (!this.loginId || !this.password) {
        this.errorMessage = '請輸入帳號和密碼'
        return
      }

      if (this.isLoginMode) {
        this.$emit('login', {
          loginId: this.loginId,
          password: this.password,
          setError: (msg) => {
            this.errorMessage = msg
          },
        })
      } else {
        // 註冊模式的額外驗證
        if (!this.realName) {
          this.errorMessage = '請輸入真實姓名'
          return
        }

        const deposit = Number(this.initialDeposit)
        if (isNaN(deposit) || deposit < 0) {
          this.errorMessage = '初始金額必須為有效數字'
          return
        }

        this.$emit('register', {
          realName: this.realName,
          loginId: this.loginId,
          password: this.password,
          initialDeposit: deposit,
          setError: (msg) => {
            this.errorMessage = msg
          },
          setSuccess: (msg, bankAccountNumber) => {
            this.successMessage = msg + `\n您的銀行帳號為：${bankAccountNumber}`
            this.isLoginMode = true
            this.realName = ''
            this.loginId = ''
            this.password = ''
            this.initialDeposit = 1000
          },
        })
      }
    },
  },
}
</script>

<style scoped>
.auth-container {
  background: white;
  padding: 40px;
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  max-width: 450px;
  margin: 50px auto;
}

.auth-container h2 {
  color: #1e3a8a;
  margin-bottom: 30px;
  text-align: center;
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
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.input-hint {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.btn {
  width: 100%;
  padding: 14px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 10px;
}

.btn:hover {
  background: #5568d3;
  transform: translateY(-2px);
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

.success-message {
  background: #d1fae5;
  color: #065f46;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
  text-align: center;
}
</style>
