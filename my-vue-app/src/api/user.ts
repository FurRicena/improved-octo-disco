import request from '@/utils/request'
import type { LoginForm, LoginResponse, RegisterForm, RegisterResponse } from '@/types/user'

// 登录接口
export function login(data: LoginForm) {
    return request<LoginResponse>({
        url: '/user/login',
        method: 'post',
        data
    })
}

// 注册接口
export function register(data: RegisterForm) {
    return request<RegisterResponse>({
        url: '/user/register',
        method: 'post',
        data
    })
}