import type {ApiResponse} from "@/lib/api/types";

export type AccountStatus =
    | "ACTIVE"
    | "BLOCKED"
    | "CLOSED";

export interface Account {
    id: string;
    userId: string;
    accountNo: string;
    currency: string;
    balance: string;
    status: AccountStatus;
    createdAt: string;
    updatedAt: string;
}

export interface AccountSummary {
    id: string;
    accountNo: string;
    currency: string;
    balance: string;
    status: AccountStatus;
}

export interface AccountHolder {
    accountNo: string;
    fullName: string;
}

export interface CreateAccountRequest {
    currency: string;
}

export type AccountResponse =
    ApiResponse<Account>;

export type AccountSummaryResponse =
    ApiResponse<AccountSummary[]>;

export type AccountHolderApiResponse =
    ApiResponse<AccountHolder>;