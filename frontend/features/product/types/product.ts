import type {ApiResponse} from "@/lib/api/types";

export type ProductCode =
    | "SAVINGS"
    | "CURRENT";

export type ProductStatus =
    | "ACTIVE"
    | "INACTIVE";

export interface Product {
    id: string;
    code: ProductCode;
    name: string;
    status: ProductStatus;
    createdAt: string;
    updatedAt: string;
}

export type GetActiveProductsResponse =
    ApiResponse<Product[]>;