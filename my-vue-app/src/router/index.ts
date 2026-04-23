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
import Layout from "@/views/Layout.vue"
import {useUserStore} from "@/stores/user.ts";
import MenuDetail from "@/views/MenuDetail.vue";

const routes = [
    { path: '/login', name: 'Login', component: UserLogin },
    { path: '/register', name: 'Register', component: UserRegister },
    {
        path: '/',
        component: Layout,
        redirect: '/order',
        children: [
            { path: 'order', name: 'OrderMenu', component: OrderMenu, meta: { title: '点菜', requiresAuth: true } },
            { path: 'my-orders', name: 'MyOrders', component: MyOrders, meta: { title: '我的订单', requiresAuth: true } },
            { path: 'order-success', name: 'OrderSuccess', component: OrderSuccess, meta: { requiresAuth: true } },
            { path: 'order-detail', name: 'OrderDetail', component: OrderDetail, meta: { requiresAuth: true } },
            { path: 'menu-detail', name: 'MenuDetail', component: MenuDetail, meta: { requiresAuth: true } },
            // 管理员页面
            { path: 'admin/menu', name: 'AdminMenu', component: AdminMenu, meta: { title: '菜品管理', requiresAuth: true, roles: ['ADMIN'] } },
            { path: 'admin/orders', name: 'AdminOrders', component: AdminOrders, meta: { title: '订单管理', requiresAuth: true, roles: ['ADMIN'] } },
            { path: 'admin/users', name: 'AdminUserManage', component: AdminUserManage, meta: { title: '用户管理', requiresAuth: true, roles: ['ADMIN'] } },
            {
                path: '/admin/statistics',
                name: 'AdminStatistics',
                component: () => import('@/views/AdminStatistics.vue'),
                meta: { requiresAuth: true, roles: ['ADMIN'] }
            },
            {
                path: '/admin/dashboard',
                name: 'AdminDashboard',
                component: () => import('@/views/AdminDashboard.vue'),
                meta: { requiresAuth: true, roles: ['ADMIN'] }
            },
            {
                path: '/admin/logs',
                name: 'AdminLogs',
                component: () => import('@/views/AdminLogs.vue'),
                meta: { requiresAuth: true, roles: ['ADMIN'] }
            },
            {
                path: '/admin/comments',
                name: 'AdminComments',
                component: () => import('@/views/AdminComments.vue'),
                meta: { requiresAuth: true, roles: ['ADMIN'] }
            }
        ]
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to) => {
    const userStore = useUserStore()
    const token = localStorage.getItem('token')
    const userInfo = userStore.userInfo

    if (to.meta.requiresAuth) {
        if (!token) {
            return '/login'
        }
        // 如果有角色要求
        if (to.meta.roles) {
            const roles = to.meta.roles as string[]
            if (!userInfo || !roles.includes(userInfo.role)) {
                return '/login'
            }
        }
        return true
    } else {
        if (to.path === '/login' && token) {
            return '/'
        } else {
            return true
        }
    }
})

export default router