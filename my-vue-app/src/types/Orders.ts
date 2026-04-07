// 订单类型
import type {OrderItem} from "@/types/OrderItem.ts";

export interface Orders {
    id: number
    userId: number
    totalPrice: number
    status: 'PENDING' | 'ACCEPTED' | 'COOKING' | 'FINISHED'   // 'PENDING', 'ACCEPTED', 'COOKING', 'FINISHED'
    createTime: string
    items?: OrderItem[]
}

// 订单项（与后端 OrderRequest.Item 对应）
export interface OrderItemRequest {
    menuId: number
    quantity: number
}

// 创建订单的请求体（对应后端 OrderRequest）
export interface CreateOrderRequest {
    userId: number           // 当前登录用户 ID
    items: OrderItemRequest[]
}

// 后端返回的订单实体（简化版）
export interface OrderResponse {
    id: number
    userId: number
    totalPrice: number
    status: 'PENDING' | 'ACCEPTED' | 'COOKING' | 'FINISHED'
    createTime: string
}