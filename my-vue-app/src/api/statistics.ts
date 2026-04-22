import request from '@/utils/request'

export interface MenuSales {
    menuId: number
    menuName: string
    price: number
    totalSold: number
}

export interface DailyOrderTrend {
    date: string        // "2026-04-01"
    orderCount: number
    totalAmount: number
}

export interface DashboardData {
    totalUsers: number
    totalOrders: number
    totalSales: number
    last7DaysTrend: DailyOrderTrend[]
    top5Menus: MenuSales[]
    statusDistribution: Record<string, number>   // e.g. { "PENDING": 3, "FINISHED": 20 }
}

export const getMenuSales = () => {
    return request<MenuSales[]>({
        url: '/admin/statistics/menu-sales',
        method: 'get'
    })
}

export const getDashboardData = () => {
    return request<DashboardData>({
        url: '/admin/statistics/dashboard',
        method: 'get'
    })
}