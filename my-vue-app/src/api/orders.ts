import request from "@/utils/request.ts"
import type {CreateOrderRequest, Orders} from "@/types/Orders.ts"
import type { PageResult} from "@/types/PageResult.ts"
import type {Menu} from "@/types/Menu.ts";

// 查询所有订单
export function getOrdersList() {
    return request<Orders[]>({
        url: '/order',
        method: 'get'
    })
}

// 创建订单
export function createOrder(data: CreateOrderRequest) {
    return request<Orders>({
        url: '/order',
        method: 'post',
        data
    })
}

// // 更新订单状态
// export function updateOrderStatus(id: number, data: String) {
//     return request<Orders>({
//         url: `/order/${id}/status`,
//         method: 'put',
//         data
//     })
// }

//查询订单详情
export function getOrderDetail(id: number) {
    return request<Orders>({
        url: `/order/${id}`,
        method: 'get'
    })
}

// 查询用户订单
export function getUserOrders(id: number) {
    return request<Orders[]>({
        url: `/order/user/${id}`,
        method: 'get'
    })
}

// 分页查询用户订单
export function getUserOrdersPage(id: number, pageNum: number, pageSize: number, status?: string) {
    return request<PageResult<Orders>>({
        url: `/order/user/${id}/page`,
        method: 'get',
        params: {
            pageNum : pageNum,
            pageSize,
            status: status === 'all' ? undefined : status  // 可选，为 undefined 时不发送该参数
        }
    })
}

// 管理员获取订单分页
export function getAdminOrdersPage(params: {
    username?: string
    status?: string
    pageNum: number
    pageSize: number
}) {
    return request({
        url: '/order/adminpage',
        method: 'get',
        params
    })
}

// 管理员修改订单状态
export function updateOrderStatus(orderId: number, status: string) {
    return request({
        url: `/order/${orderId}/status`,
        method: 'put',
        params: { status }
    })
}

// 1. AI 智能推荐（非流式，返回菜品列表）
export function aiRecommend(message: string) {
    return request<Menu[]>({
        url: '/ai/recommend',
        method: 'post',
        data: { message }
    })
}

export const exportOrders = (params?: {
    username?: string
    status?: string
}) => {
    return request({
        url: `/order/export`,
        method: 'get',
        params,
        responseType: 'blob'
    })
}

// 2. 流式聊天（获取 SSE 流，返回 Fetch Response，需前端自行处理）
// 注意：流式接口通常不经过 axios 拦截器，直接使用 fetch
// export function aiStreamChat(message: string) {
//     const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
//     return fetch(`${baseURL}/ai/stream-chat?msg=${encodeURIComponent(message)}`, {
//         method: 'GET',
//         headers: {
//             'Authorization': `Bearer ${localStorage.getItem('token')}`
//         }
//     })
// }