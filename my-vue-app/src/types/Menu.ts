export interface Menu {
    id?: number
    name: string
    price: number
    category?: string
    description?: string
    imageUrl?: string
    status?: number
}

export interface AiRecommendResponse {
    menus: Menu[]           // 注意：字段名从 recommendations 变为 menus（与后端对应）
    speakText: string
}