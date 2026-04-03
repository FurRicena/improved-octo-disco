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
            ElMessage.error(res.data.message || "请求失败")
            return Promise.reject(new Error(res.data.message))
        }
        return res.data
    },
    (err) => {
        console.log(err)
        return Promise.reject(err)
    }
)

export default request