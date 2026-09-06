import {apiClient} from "@/lib/api/axios";
import {API_ENDPOINTS} from "@/lib/constants/api";
import type {ApiResponse} from "@/lib/api/types";

import type {
    Account,
    AccountHolder,
    AccountSummary,
    CreateAccountRequest,
} from "../types/account";

export const accountApi = {
    create: async (
        request: CreateAccountRequest,
    ): Promise<ApiResponse<Account>> => {
        const response = await apiClient.post<
            ApiResponse<Account>
        >(
            API_ENDPOINTS.ACCOUNT.BASE,
            request,
        );

        return response.data;
    },

    getById: async (
        accountId: string,
    ): Promise<ApiResponse<Account>> => {
        const response = await apiClient.get<
            ApiResponse<Account>
        >(
            API_ENDPOINTS.ACCOUNT.BY_ID(accountId),
        );

        return response.data;
    },

    getByAccountNo: async (
        accountNo: string,
    ): Promise<ApiResponse<Account>> => {
        const response = await apiClient.get<
            ApiResponse<Account>
        >(
            API_ENDPOINTS.ACCOUNT.BY_NUMBER(
                accountNo,
            ),
        );

        return response.data;
    },

    getHolder: async (
        accountNo: string,
    ): Promise<ApiResponse<AccountHolder>> => {
        const response = await apiClient.get<
            ApiResponse<AccountHolder>
        >(
            API_ENDPOINTS.ACCOUNT.HOLDER(
                accountNo,
            ),
        );

        return response.data;
    },

    getMyAccounts: async (): Promise<
        ApiResponse<AccountSummary[]>
    > => {
        const response = await apiClient.get<
            ApiResponse<AccountSummary[]>
        >(
            API_ENDPOINTS.ACCOUNT.BASE,
        );

        return response.data;
    },

    block: async (
        accountId: string,
    ): Promise<ApiResponse<void>> => {
        const response = await apiClient.post<
            ApiResponse<void>
        >(
            API_ENDPOINTS.ACCOUNT.BLOCK(accountId),
        );

        return response.data;
    },

    activate: async (
        accountId: string,
    ): Promise<ApiResponse<void>> => {
        const response = await apiClient.post<
            ApiResponse<void>
        >(
            API_ENDPOINTS.ACCOUNT.ACTIVATE(
                accountId,
            ),
        );

        return response.data;
    },

    close: async (
        accountId: string,
    ): Promise<ApiResponse<void>> => {
        const response = await apiClient.post<
            ApiResponse<void>
        >(
            API_ENDPOINTS.ACCOUNT.CLOSE(accountId),
        );

        return response.data;
    },
};