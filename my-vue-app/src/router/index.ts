import { createRouter, createWebHistory } from 'vue-router'

// 引入页面
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'

const routes = [
    {
        path: '/login',
        component: Login
    },
    {
        path: '/register',
        component: Register
    },
    {
        path: '/',
        redirect: '/login' // 默认跳登录页
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router