import type {ApiResponse} from "@/lib/api/types";
import {Currency} from "@/lib/constants/currency";

export interface ExecuteWithdrawalRequest {
    lookupCode: string;
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
    currency: Currency;
    executedAt: string;
}

export type ExecuteWithdrawalApiResponse =
    ApiResponse<ExecuteWithdrawalResponse>;