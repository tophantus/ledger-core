import {Currency} from "@/lib/constants/currency";

export type AccountStatus =
    | "ACTIVE"
    | "BLOCKED"
    | "CLOSED";

export interface Account {
    id: string;
    userId: string;
    accountNo: string;
    productId: string;
    currency: Currency;
    balance: string;
    holdAmount: string;
    availableBalance: string;
    status: AccountStatus;
    createdAt: string;
    updatedAt: string;
}

export interface AccountSummary {
    id: string;
    accountNo: string;
    productId: string;
    currency: Currency;
    balance: string;
    holdAmount: string;
    availableBalance: string;
    status: AccountStatus;
}

export interface AccountHolder {
    accountNo: string;
    fullName: string;
}

export interface CreateAccountRequest {
    productId: string;
    currency: Currency;
}