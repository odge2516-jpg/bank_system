<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>

<script>
import { useBankStore } from './stores/bank'

export default {
  name: 'App',
  created() {
    const store = useBankStore()
    store.$subscribe((mutation, state) => {
      localStorage.setItem('bank_system_data', JSON.stringify(state))
    })
  },
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Inter', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
