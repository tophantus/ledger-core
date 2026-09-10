import type {
    ApiResponse,
    PageResponse,
} from "@/lib/api/types";

export type WithdrawalRequestStatus =
    | "PENDING"
    | "CONFIRMED"
    | "EXPIRED"
    | "CANCELLED";

export type WithdrawalIntentStatus =
    | "READY"
    | "COMPLETED"
    | "EXPIRED"
    | "CANCELLED";

export interface CreateWithdrawalRequest {
    accountId: string;
    amount: string;
    currency: string;
}

export interface WithdrawalRequestResponse {
    requestId: string;
    accountId: string;
    amount: string;
    currency: string;
    status: WithdrawalRequestStatus;
    expiresAt: string;
}

export interface ConfirmWithdrawalRequest {
    otp: string;
}

export interface ConfirmWithdrawalRequestResponse {
    requestId: string;
    requestStatus: WithdrawalRequestStatus;
    intentId: string;
    withdrawalReference: string;
    amount: string;
    currency: string;
    intentExpiresAt: string;
}

export interface WithdrawalIntent {
    id: string;
    accountId: string;
    withdrawalReference: string;
    amount: string;
    currency: string;
    status: WithdrawalIntentStatus;
    expiresAt: string;
    createdAt: string;
    completedAt: string | null;
}

export interface WithdrawalIntentFilters {
    accountId?: string;
    status?: WithdrawalIntentStatus;
    page?: number;
    size?: number;
}

export type CreateWithdrawalResponse =
    ApiResponse<WithdrawalRequestResponse>;

export type ConfirmWithdrawalResponse =
    ApiResponse<ConfirmWithdrawalRequestResponse>;

export type WithdrawalIntentPageResponse =
    ApiResponse<PageResponse<WithdrawalIntent>>;