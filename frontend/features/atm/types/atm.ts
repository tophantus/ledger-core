import type {ApiResponse} from "@/lib/api/types";

export interface ExecuteWithdrawalRequest {
    withdrawalReference: string;
    withdrawalCode: string;
    amount: string;
}

export interface ExecuteWithdrawalResponse {
    executionId: string;
    withdrawalIntentId: string;
    transactionId: string;
    atmTerminalId: string;
    withdrawalReference: string;
    amount: string;
    currency: string;
    executedAt: string;
}

export type ExecuteWithdrawalApiResponse =
    ApiResponse<ExecuteWithdrawalResponse>;