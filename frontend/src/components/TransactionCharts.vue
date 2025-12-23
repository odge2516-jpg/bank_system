<template>
  <div class="charts-container">
    <h3>📊 交易分析</h3>

    <div class="charts-grid">
      <!-- 收支趨勢圖 -->
      <div class="chart-card">
        <h4>收支趨勢（最近7天）</h4>
        <div class="chart-wrapper">
          <LineChart v-if="lineChartData" :data="lineChartData" :options="lineChartOptions" />
          <div v-else class="no-data">暫無交易資料</div>
        </div>
      </div>

      <!-- 交易類型分布圖 -->
      <div class="chart-card">
        <h4>交易類型分布</h4>
        <div class="chart-wrapper">
          <Doughnut
            v-if="doughnutChartData"
            :data="doughnutChartData"
            :options="doughnutChartOptions"
          />
          <div v-else class="no-data">暫無交易資料</div>
        </div>
      </div>

      <!-- 每月收支統計 -->
      <div class="chart-card">
        <h4>每月收支統計</h4>
        <div class="chart-wrapper">
          <Bar v-if="barChartData" :data="barChartData" :options="barChartOptions" />
          <div v-else class="no-data">暫無交易資料</div>
        </div>
      </div>

      <!-- 交易統計卡片 -->
      <div class="stats-card">
        <h4>交易統計</h4>
        <div class="stats-list">
          <div class="stat-item">
            <span class="stat-label">總收入</span>
            <span class="stat-value positive">+NT$ {{ formatAmount(totalIncome) }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">總支出</span>
            <span class="stat-value negative">-NT$ {{ formatAmount(totalExpense) }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">淨收益</span>
            <span class="stat-value" :class="netProfit >= 0 ? 'positive' : 'negative'">
              {{ netProfit >= 0 ? '+' : '' }}NT$ {{ formatAmount(netProfit) }}
            </span>
          </div>
          <div class="stat-item">
            <span class="stat-label">交易筆數</span>
            <span class="stat-value">{{ transactions.length }} 筆</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Line as LineChart, Doughnut, Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler,
)

export default {
  name: 'TransactionCharts',
  components: {
    LineChart,
    Doughnut,
    Bar,
  },
  props: {
    transactions: {
      type: Array,
      default: () => [],
    },
  },
  computed: {
    // 計算總收入
    totalIncome() {
      return this.transactions.filter((t) => t.amount > 0).reduce((sum, t) => sum + t.amount, 0)
    },
    // 計算總支出
    totalExpense() {
      return Math.abs(
        this.transactions.filter((t) => t.amount < 0).reduce((sum, t) => sum + t.amount, 0),
      )
    },
    // 計算淨收益
    netProfit() {
      return this.totalIncome - this.totalExpense
    },
    // 折線圖數據 - 最近7天
    lineChartData() {
      if (this.transactions.length === 0) return null

      const last7Days = this.getLast7Days()
      const dailyData = this.getDailyData(last7Days)

      return {
        labels: last7Days.map((date) => this.formatDate(date)),
        datasets: [
          {
            label: '收入',
            data: dailyData.income,
            borderColor: '#10b981',
            backgroundColor: 'rgba(16, 185, 129, 0.1)',
            fill: true,
            tension: 0.4,
          },
          {
            label: '支出',
            data: dailyData.expense,
            borderColor: '#ef4444',
            backgroundColor: 'rgba(239, 68, 68, 0.1)',
            fill: true,
            tension: 0.4,
          },
        ],
      }
    },
    // 圓餅圖數據 - 交易類型分布
    doughnutChartData() {
      if (this.transactions.length === 0) return null

      const typeStats = this.getTransactionTypeStats()

      return {
        labels: Object.keys(typeStats),
        datasets: [
          {
            data: Object.values(typeStats),
            backgroundColor: ['#10b981', '#ef4444', '#3b82f6', '#f59e0b', '#8b5cf6', '#ec4899'],
            borderWidth: 2,
            borderColor: '#fff',
          },
        ],
      }
    },
    // 柱狀圖數據 - 每月收支
    barChartData() {
      if (this.transactions.length === 0) return null

      const monthlyData = this.getMonthlyData()

      return {
        labels: monthlyData.labels,
        datasets: [
          {
            label: '收入',
            data: monthlyData.income,
            backgroundColor: '#10b981',
          },
          {
            label: '支出',
            data: monthlyData.expense,
            backgroundColor: '#ef4444',
          },
        ],
      }
    },
    // 圖表選項
    lineChartOptions() {
      return {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top',
          },
          tooltip: {
            callbacks: {
              label: (context) => {
                return `${context.dataset.label}: NT$ ${context.parsed.y.toLocaleString('zh-TW')}`
              },
            },
          },
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: (value) => 'NT$ ' + value.toLocaleString('zh-TW'),
            },
          },
        },
      }
    },
    doughnutChartOptions() {
      return {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'right',
          },
          tooltip: {
            callbacks: {
              label: (context) => {
                const total = context.dataset.data.reduce((a, b) => a + b, 0)
                const percentage = ((context.parsed / total) * 100).toFixed(1)
                return `${context.label}: ${context.parsed} 筆 (${percentage}%)`
              },
            },
          },
        },
      }
    },
    barChartOptions() {
      return {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top',
          },
          tooltip: {
            callbacks: {
              label: (context) => {
                return `${context.dataset.label}: NT$ ${context.parsed.y.toLocaleString('zh-TW')}`
              },
            },
          },
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: (value) => 'NT$ ' + value.toLocaleString('zh-TW'),
            },
          },
        },
      }
    },
  },
  methods: {
    formatAmount(amount) {
      return Math.abs(amount).toLocaleString('zh-TW')
    },
    formatDate(date) {
      return `${date.getMonth() + 1}/${date.getDate()}`
    },
    getLast7Days() {
      const days = []
      for (let i = 6; i >= 0; i--) {
        const date = new Date()
        date.setDate(date.getDate() - i)
        date.setHours(0, 0, 0, 0)
        days.push(date)
      }
      return days
    },
    getDailyData(days) {
      const income = new Array(7).fill(0)
      const expense = new Array(7).fill(0)

      this.transactions.forEach((t) => {
        const transactionDate = new Date(t.timestamp)
        transactionDate.setHours(0, 0, 0, 0)

        const dayIndex = days.findIndex((d) => d.getTime() === transactionDate.getTime())
        if (dayIndex !== -1) {
          if (t.amount > 0) {
            income[dayIndex] += t.amount
          } else {
            expense[dayIndex] += Math.abs(t.amount)
          }
        }
      })

      return { income, expense }
    },
    getTransactionTypeStats() {
      const stats = {}
      this.transactions.forEach((t) => {
        stats[t.type] = (stats[t.type] || 0) + 1
      })
      return stats
    },
    getMonthlyData() {
      const monthlyStats = {}

      this.transactions.forEach((t) => {
        const date = new Date(t.timestamp)
        const monthKey = `${date.getFullYear()}/${date.getMonth() + 1}`

        if (!monthlyStats[monthKey]) {
          monthlyStats[monthKey] = { income: 0, expense: 0 }
        }

        if (t.amount > 0) {
          monthlyStats[monthKey].income += t.amount
        } else {
          monthlyStats[monthKey].expense += Math.abs(t.amount)
        }
      })

      const sortedMonths = Object.keys(monthlyStats).sort()
      const last6Months = sortedMonths.slice(-6)

      return {
        labels: last6Months,
        income: last6Months.map((m) => monthlyStats[m].income),
        expense: last6Months.map((m) => monthlyStats[m].expense),
      }
    },
  },
}
</script>

<style scoped>
.charts-container {
  margin-top: 30px;
}

.charts-container h3 {
  color: #1e3a8a;
  margin-bottom: 20px;
  font-size: 24px;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.chart-card,
.stats-card {
  background: white;
  padding: 20px;
  border-radius: 15px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.chart-card h4,
.stats-card h4 {
  color: #334155;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 600;
}

.chart-wrapper {
  height: 300px;
  position: relative;
}

.no-data {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #94a3b8;
  font-size: 14px;
}

.stats-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f8fafc;
  border-radius: 8px;
}

.stat-label {
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
}

.stat-value.positive {
  color: #10b981;
}

.stat-value.negative {
  color: #ef4444;
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
