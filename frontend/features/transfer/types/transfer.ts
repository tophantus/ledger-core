import {Currency} from "@/lib/constants/currency";

export interface CreateTransferIntentRequest {
    sourceAccountId: string;
    destinationAccountNo: string;
    amount: string;
    currency: Currency;
    reference: string;
    description?: string;
}

export enum TransferIntentStatus {
    PENDING = "PENDING",
    CONFIRMED = "CONFIRMED",
    EXPIRED = "EXPIRED",
    CANCELLED = "CANCELLED",
}

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