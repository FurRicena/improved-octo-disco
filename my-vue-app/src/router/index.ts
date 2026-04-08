import { createRouter, createWebHistory } from 'vue-router'

// 引入页面
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import Menu from "@/views/Menu.vue"
import UserManage from "@/views/UserManage.vue"
import OrderMenu from "@/views/OrderMenu.vue"

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
    },
    {
        path: '/menu',
        component: Menu
    },
    {
        path: '/userManage',
        component: UserManage
    },
    {
        path: '/orderMenu',
        component: OrderMenu
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router