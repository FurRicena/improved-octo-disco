import { createRouter, createWebHistory } from 'vue-router'

// 引入页面
import UserLogin from '@/views/UserLogin.vue'
import UserRegister from '@/views/UserRegister.vue'
import AdminMenu from "@/views/AdminMenu.vue"
import AdminUserManage from "@/views/AdminUserManage.vue"
import OrderMenu from "@/views/OrderMenu.vue"
import OrderSuccess from "@/views/OrderSuccess.vue"
import MyOrders from "@/views/MyOrders.vue"
import OrderDetail from "@/views/OrderDetail.vue"
import AdminOrders from "@/views/AdminOrders.vue"

const routes = [
    {
        path: '/login',
        component: UserLogin
    },
    {
        path: '/register',
        component: UserRegister
    },
    {
        path: '/',
        redirect: '/login' // 默认跳登录页
    },
    {
        path: '/adminMenu',
        component: AdminMenu
    },
    {
        path: '/adminUserManage',
        component: AdminUserManage
    },
    {
        path: '/orderMenu',
        component: OrderMenu
    },
    {
        path: '/orderSuccess',
        component: OrderSuccess
    },
    {
        path: '/myOrders',
        component: MyOrders
    },
    {
        path: '/orderDetail',
        component: OrderDetail
    },
    {
        path: '/adminOrders',
        component: AdminOrders
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router