import { createRouter, createWebHistory } from 'vue-router'
import { useBankStore } from '../stores/bank'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import AdminDashboardView from '../views/AdminDashboardView.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'login',
            component: LoginView
        },
        {
            path: '/dashboard',
            name: 'dashboard',
            component: DashboardView,
            meta: { requiresAuth: true, role: 'user' }
        },
        {
            path: '/admin',
            name: 'admin',
            component: AdminDashboardView,
            meta: { requiresAuth: true, role: 'admin' }
        }
    ]
})

router.beforeEach((to, from, next) => {
    const store = useBankStore()

    // 檢查需要認證的頁面
    if (to.meta.requiresAuth) {
        if (!store.currentUser) {
            // 未登入，導向登入頁
            next({ path: '/', replace: true })
            return
        }

        // 檢查角色權限
        if (to.meta.role && to.meta.role !== store.currentUser.role) {
            // 角色不符，導向對應的頁面
            if (store.currentUser.role === 'admin') {
                next({ path: '/admin', replace: true })
            } else {
                next({ path: '/dashboard', replace: true })
            }
            return
        }
    }

    next()
})

export default router
