// stores/user.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi } from '@/api/user'
import type { LoginForm, User } from '@/types/User'

export const useUserStore = defineStore('user', () => {
    const token = ref<string | null>(localStorage.getItem('token'))
    const stored = localStorage.getItem('userInfo')
    const initialUser = stored ? (JSON.parse(stored) as User) : null
    const userInfo = ref<User | null>(initialUser)

    async function login(loginForm: LoginForm) {
        const res = await loginApi(loginForm)
        // 假设后端返回 { code:200, data: { token, id, username, role } }
        token.value = res.data.token
        userInfo.value = res.data
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('userInfo', JSON.stringify(res.data))
        return res.data
    }

    function logout() {
        token.value = null
        userInfo.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
    }

    return { token, userInfo, login, logout }
})