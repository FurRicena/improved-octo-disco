import axios from 'axios'
import type { AxiosResponse } from 'axios'
import {ElMessage} from "element-plus";

const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 5000
})

// 响应拦截器（TS写法）
request.interceptors.response.use(
    (res: AxiosResponse) => {

        // 判断是否为blob型请求
        if (res.config.responseType === 'blob') {
            return res;
        }

        if(res.data.code !== 200){
            ElMessage.error(res.data.msg || "拦截器处统一失败")
            return Promise.reject(new Error(res.data.message))
        }
        if (res.data.code === 401 || res.data.code === 403) {
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            window.location.href = '/login'
        }
        return res.data
    },
    (err) => {
        console.log(err)
        return Promise.reject(err)
    }
)

// 请求拦截器：自动添加 Authorization 请求头
request.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

export default request