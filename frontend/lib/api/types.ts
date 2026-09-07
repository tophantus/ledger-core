export interface ApiResponse<T> {
    success: boolean;
    data: T;
    message: string;
    code?: string;
}

export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}