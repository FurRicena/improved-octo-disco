export interface PageResult<T> {
    content: T[];        // 数据列表
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;      // 当前页码（从0开始）
    first: boolean;
    last: boolean;
    empty: boolean;
}