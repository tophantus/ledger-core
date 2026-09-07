import type {ApiResponse, PageResponse} from "@/lib/api/types";

export type AccountStatus =
    | "ACTIVE"
    | "BLOCKED"
    | "CLOSED";

export interface AdminAccount {
    id: string;
    userId: string;
    accountNo: string;
    currency: string;
    balance: string;
    status: AccountStatus;
    ledgerAccountId: string;
    createdAt: string;
    updatedAt: string;
}

export interface AdminAccountFilters {
    accountNo?: string;
    status?: AccountStatus;
    currency?: string;
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
    currency: string;
    balance: string;
    status: AccountStatus;
    ledgerAccountId: string;
    createdAt: string;
    updatedAt: string;
    user: AdminAccountUserInfo;
}

export type AdminAccountPageResponse =
    ApiResponse<PageResponse<AdminAccount>>;