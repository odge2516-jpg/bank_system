<template>
  <div class="login-view">
    <AuthForm @login="handleLogin" @register="handleRegister" />
  </div>
</template>

<script>
import { mapActions, mapState } from 'pinia'
import { useBankStore } from '../stores/bank'
import AuthForm from '../components/AuthForm.vue'

export default {
  name: 'LoginView',
  components: {
    AuthForm,
  },
  computed: {
    ...mapState(useBankStore, ['currentUser']),
  },
  mounted() {
    // 如果已經登入，直接跳轉到儀表板
    if (this.currentUser) {
      this.$router.replace('/dashboard')
    }
  },
  methods: {
    ...mapActions(useBankStore, ['login', 'register', 'formatAccountNumber']),

    async handleRegister({ realName, loginId, password, initialDeposit, setError, setSuccess }) {
      try {
        const newUser = await this.register(realName, loginId, password, initialDeposit)
        const formattedAccountNumber = this.formatAccountNumber(newUser.id)
        setSuccess('註冊成功！', formattedAccountNumber)
      } catch (error) {
        setError(error.message)
      }
    },

    async handleLogin({ loginId, password, setError }) {
      try {
        const user = await this.login(loginId, password)
        // 根據角色導向不同頁面
        if (user.role === 'admin') {
          this.$router.push('/admin')
        } else {
          this.$router.push('/dashboard')
        }
      } catch (error) {
        setError(error.message)
      }
    },
  },
}
</script>

<style scoped>
.login-view {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(-45deg, #1e3a8a, #3b82f6, #667eea, #764ba2);
  background-size: 400% 400%;
  animation: gradient 15s ease infinite;
}

@keyframes gradient {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}
</style>
