import request from '@/utils/request'
import type { Comment } from '@/types/Comment'
import type {PageResult} from "@/types/PageResult.ts";

export const addComment = (data: { menuId: number; rating: number; content: string }) => {
    return request<Comment>({
        url: '/comment/add',
        method: 'post',
        data
    })
}

export const getCommentsByMenu = (menuId: number, page: number, size: number) => {
    return request<PageResult<Comment>>({
        url: `/comment/menu/${menuId}`,
        method: 'get',
        params: { page, size }
    })
}

export const getAverageRating = (menuId: number) => {
    return request<number>({
        url: `/comment/menu/${menuId}/rating`,
        method: 'get'
    })
}

// 管理员
export const getAdminComments = (page: number, size: number) => {
    return request<{ content: Comment[]; totalElements: number }>({
        url: '/comment/list',
        method: 'get',
        params: { page, size }
    })
}

export const updateCommentStatus = (commentId: number, status: number) => {
    return request({
        url: `/comment/${commentId}/status`,
        method: 'put',
        params: { status }
    })
}

export const deleteComment = (commentId: number) => {
    return request({
        url: `/comment/${commentId}`,
        method: 'delete'
    })
}