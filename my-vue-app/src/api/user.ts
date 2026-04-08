import request from '@/utils/request'
import type {LoginForm, LoginResponse, RegisterForm, RegisterResponse, User} from '@/types/User.ts'

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

// 获取所有用户
export function getUserList() {
    return request<User[]>({
        url: '/user',
        method: 'get'
    })
}

// 按id修改用户
export function updateUser(id: number, data: Partial<User>) {
    return request<User>({
        url: `/user/${id}`,
        method: 'put',
        data
    })
}

// 用户删除
export function deleteUser(id: number) {
    return request<void>({
        url: `/user/${id}`,
        method: 'delete'
    })
}