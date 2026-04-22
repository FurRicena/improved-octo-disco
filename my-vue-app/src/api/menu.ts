import request from '@/utils/request'
import type { Menu } from '@/types/Menu'

// 获取所有上架菜单列表
export function getMenuList() {
    return request<Menu[]>({
        url: '/menu/available',
        method: 'get'
    })
}

// 根据ID获取菜单
export function getMenuById(id: number) {
    return request<Menu>({
        url: `/menu/${id}`,
        method: 'get'
    })
}

// 新增菜单
export function addMenu(data: Menu) {
    return request<Menu>({
        url: '/menu/addMenu',
        method: 'post',
        data
    })
}

// 修改菜单
export function updateMenu(id: number, data: Menu) {
    return request<Menu>({
        url: `/menu/${id}`,
        method: 'put',
        data
    })
}

// 删除菜单
export function deleteMenu(id: number) {
    return request<void>({
        url: `/menu/${id}`,
        method: 'delete'
    })
}

// 获取菜品分页
export function getAdminMenuPage(params: {
    name?: string
    category?: string
    status?: number
    pageNum: number
    pageSize: number
}) {
    return request({
        url: '/menu/page',
        method: 'get',
        params
    })
}

export const exportMenus = (params?: {
    name?: string
    category?: string
    status?: number
}) => {
    return request({
        url: '/menu/export',
        params,
        responseType: 'blob'
    })
}