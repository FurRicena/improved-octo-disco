import request from '@/utils/request'

export interface MenuSales {
    menuId: number
    menuName: string
    price: number
    totalSold: number
}

export const getMenuSales = () => {
    return request<MenuSales[]>({
        url: '/admin/statistics/menu-sales',
        method: 'get'
    })
}