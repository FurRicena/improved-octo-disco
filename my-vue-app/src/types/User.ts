// 用户类型
export interface User{
    id: number
    username: string
    password?: string   // 列表中不展示，编辑时可修改
    role: string        // 'ADMIN' 或 'USER'
    createTime?: string
}

// 登录
export interface LoginForm {
    username: string
    password: string
}

export interface LoginResponse {
    token: string
    type: string
    id: number
    username: string
    role: string
}

// 注册
export interface RegisterForm {
    username: string
    password: string
    role: string
}

export interface RegisterResponse {
    message: string
}