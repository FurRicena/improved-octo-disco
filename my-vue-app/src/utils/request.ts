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
        if(res.data.code !== 200){
            ElMessage.error(res.data.msg || "请求失败")
            return Promise.reject(new Error(res.data.message))
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