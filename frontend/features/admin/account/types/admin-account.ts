import {Currency} from "@/lib/constants/currency";

export type AccountStatus =
    | "ACTIVE"
    | "BLOCKED"
    | "CLOSED";

export interface AdminAccount {
    id: string;
    userId: string;
    accountNo: string;
    productId: string;
    currency: Currency;
    balance: string;
    holdAmount: string;
    availableBalance: string;
    status: AccountStatus;
    ledgerAccountId: string;
    createdAt: string;
    updatedAt: string;
}

export interface AdminAccountFilters {
    accountNo?: string;
    status?: AccountStatus;
    currency?: Currency;
    userId?: string;
    page?: number;
    size?: number;
}

export interface AdminAccountUserInfo {
    id: string;
    email: string;
    fullName: string;
    avatarUrl: string | null;
}

export interface AdminAccountDetail {
    id: string;
    accountNo: string;
    productId: string;
    currency: Currency;
    balance: string;
    holdAmount: string;
    availableBalance: string;
    status: AccountStatus;
    ledgerAccountId: string;
    createdAt: string;
    updatedAt: string;
    user: AdminAccountUserInfo;
}