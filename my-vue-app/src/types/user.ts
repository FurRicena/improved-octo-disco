// 登录
export interface LoginForm {
    username: string
    password: string
}

export interface LoginResponse {
    token: string
    userId: number
}

// 注册
export interface RegisterForm {
    username: string
    password: string
}

export interface RegisterResponse {
    message: string
}