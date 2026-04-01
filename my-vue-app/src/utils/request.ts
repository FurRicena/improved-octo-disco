import axios from 'axios'
import type { AxiosResponse } from 'axios'

const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 5000
})

// 响应拦截器（TS写法）
request.interceptors.response.use(
    (res: AxiosResponse) => {
        return res.data
    },
    (err) => {
        console.log(err)
        return Promise.reject(err)
    }
)

export default request