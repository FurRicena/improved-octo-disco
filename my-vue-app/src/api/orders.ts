import request from "@/utils/request.ts"
import type {CreateOrderRequest, Orders} from "@/types/Orders.ts"
import type { PageResult} from "@/types/PageResult.ts"

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

// 更新订单状态
export function updateOrderStatus(id: number, data: String) {
    return request<Orders>({
        url: `/order/${id}/status`,
        method: 'put',
        data
    })
}

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