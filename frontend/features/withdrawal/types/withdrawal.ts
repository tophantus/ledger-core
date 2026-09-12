import {Currency} from "@/lib/constants/currency";

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
    currency: Currency;
}

export interface WithdrawalRequestResponse {
    requestId: string;
    accountId: string;
    amount: string;
    currency: Currency;
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
    currency: Currency;
    intentExpiresAt: string;
}

export interface WithdrawalIntent {
    id: string;
    accountId: string;
    withdrawalReference: string;
    amount: string;
    currency: Currency;
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