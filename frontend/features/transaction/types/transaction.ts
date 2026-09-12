import {Currency} from "@/lib/constants/currency";

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
    currency: Currency;
    description: string | null;
    incoming: boolean;
    createdAt: string;
    completedAt: string | null;
}

export interface TransactionFilters {
    accountId?: string;
    status?: TransactionStatus;
    type?: TransactionType;
    currency?: Currency;
    from?: string;
    to?: string;
    page?: number;
    size?: number;
}

export interface CreateTransferIntentRequest {
    sourceAccountId: string;
    destinationAccountNo: string;
    amount: string;
    currency: Currency;
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
    currency: Currency;
    reference: string;
    status: TransferIntentStatus;
    expiresAt: string;
    createdAt: string;
}

export interface ConfirmTransferRequest {
    intentId: string;
    otp: string;
}