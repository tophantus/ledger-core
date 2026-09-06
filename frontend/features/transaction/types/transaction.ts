import type {ApiResponse} from "@/lib/api/types";

export type TransactionType =
    | "TRANSFER"
    | "DEPOSIT"
    | "WITHDRAW"
    | "FEE"
    | "REFUND";

export type TransactionStatus =
    | "PENDING"
    | "COMPLETED"
    | "FAILED"
    | "CANCELLED";

export interface Transaction {
    id: string;
    reference: string;
    type: TransactionType;
    status: TransactionStatus;
    sourceAccountId: string | null;
    destinationAccountId: string | null;
    amount: string;
    currency: string;
    description: string | null;
    createdAt: string;
    completedAt: string | null;
}

export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export interface TransactionFilters {
    status?: TransactionStatus;
    type?: TransactionType;
    currency?: string;
    from?: string;
    to?: string;
    page?: number;
    size?: number;
}

export interface CreateTransferIntentRequest {
    sourceAccountId: string;
    destinationAccountNo: string;
    amount: string;
    currency: string;
    reference: string;
    description?: string;
}

export type TransferIntentStatus =
    | "PENDING"
    | "CONFIRMED"
    | "EXPIRED"
    | "CANCELLED";

export interface CreateTransferIntentResult {
    intentId: string;
    sourceAccountId: string;
    destinationAccountId: string;
    amount: string;
    currency: string;
    reference: string;
    status: TransferIntentStatus;
    expiresAt: string;
    createdAt: string;
}

export interface ConfirmTransferRequest {
    intentId: string;
    otp: string;
}

export interface WithdrawMoneyRequest {
    sourceAccountId: string;
    amount: string;
    currency: string;
    reference: string;
    description?: string;
}

export type TransactionApiResponse =
    ApiResponse<Transaction>;

export type TransactionPageResponse =
    ApiResponse<PageResponse<Transaction>>;

export type TransferIntentApiResponse =
    ApiResponse<CreateTransferIntentResult>;