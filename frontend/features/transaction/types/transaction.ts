import {Currency} from "@/lib/constants/currency";

export enum TransactionType {
    TRANSFER = "TRANSFER",
    DEPOSIT = "DEPOSIT",
    WITHDRAW = "WITHDRAW",
    CARD_PAYMENT = "CARD_PAYMENT",
    CASH_ADVANCE = "CASH_ADVANCE",
    CREDIT_PAYMENT = "CREDIT_PAYMENT",
    FEE = "FEE",
    REFUND = "REFUND",
    INTEREST = "INTEREST",
}

export enum TransactionStatus {
    PENDING = "PENDING",
    COMPLETED = "COMPLETED",
    FAILED = "FAILED",
    CANCELLED = "CANCELLED",
}

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
    creditFacilityId?: string;
    status?: TransactionStatus;
    type?: TransactionType;
    currency?: Currency;
    from?: string;
    to?: string;
    page?: number;
    size?: number;
}

