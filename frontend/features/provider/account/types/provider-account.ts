import type {ApiResponse} from "@/lib/api/types";
import type {Currency} from "@/lib/constants/currency";

import type {AccountStatus} from "@/features/account/types/account";

export interface ProviderCredentials {
    clientId: string;
    credential: string;
}

export interface ProviderAccountSummary {
    id: string;
    accountNo: string;
    productId: string;
    currency: Currency;
    balance: string;
    holdAmount: string;
    availableBalance: string;
    status: AccountStatus;
}

export interface ProviderAccount {
    id: string;
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

export type GetProviderAccountsResponse =
    ApiResponse<ProviderAccountSummary[]>;

export type GetProviderAccountResponse =
    ApiResponse<ProviderAccount>;