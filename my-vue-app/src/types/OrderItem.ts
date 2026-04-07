export interface OrderItem {
    id: number
    orderId: number
    menuId: number
    menuName?: string   // 关联菜品名称
    quantity: number
    price: number
}