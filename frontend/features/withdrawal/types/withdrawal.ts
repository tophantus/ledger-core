import {Currency} from "@/lib/constants/currency";

export enum WithdrawalRequestStatus {
    PENDING = "PENDING",
    CONFIRMED = "CONFIRMED",
    EXPIRED = "EXPIRED",
    CANCELLED = "CANCELLED"
}

export enum WithdrawalIntentStatus {
    READY = "READY",
    COMPLETED = "COMPLETED",
    EXPIRED = "EXPIRED",
    CANCELLED = "CANCELLED"
}

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