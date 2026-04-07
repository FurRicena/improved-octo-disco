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