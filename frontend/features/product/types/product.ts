import type {ApiResponse} from "@/lib/api/types";

export enum ProductCode {
    SAVINGS = "SAVINGS",
    CURRENT = "CURRENT",
}

export enum ProductStatus {
    ACTIVE = "ACTIVE",
    INACTIVE = "INACTIVE",
}

export enum ProductType {
    DEPOSIT = "DEPOSIT",
    CREDIT = "CREDIT",
}

export interface Product {
    id: string;
    code: ProductCode;
    name: string;
    type: ProductType;
    status: ProductStatus;
    createdAt: string;
    updatedAt: string;
}

export interface GetActiveProductsData {
    products: Product[];
}

export type GetActiveProductsResponse =
    ApiResponse<GetActiveProductsData>;