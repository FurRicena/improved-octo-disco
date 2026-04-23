import request from '@/utils/request'
import type {PageResult} from "@/types/PageResult.ts";

// 日志记录类型
export interface Log {
    id: number
    operator: string      // 操作人
    operation: string     // 操作类型（如 "登录"、"新增菜品"）
    target: string        // 操作对象（如 "菜品ID:10"）
    detail: string        // 详情
    ip: string            // IP地址
    createTime: string    // 操作时间
}

export interface LogQueryParams {
    operator?: string     // 可选：按操作人筛选
    operation?: string    // 可选：按操作类型筛选
    pageNum: number
    pageSize: number
}

// 获取日志列表
export const getLogs = (params: {
    operator?: string     // 可选：按操作人筛选
    operation?: string    // 可选：按操作类型筛选
    pageNum: number
    pageSize: number
}) => {
    return request<PageResult<Log>>({
        url: '/admin/logs',
        method: 'get',
        params
    })
}