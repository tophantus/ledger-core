import {apiClient} from "@/lib/api/axios";
import {API_ENDPOINTS} from "@/lib/constants/api";
import type {ApiResponse, PageResponse} from "@/lib/api/types";

import type {
    ConfirmTransferRequest,
    CreateTransferIntentRequest,
    CreateTransferIntentResult,
    Transaction,
    TransactionFilters,
} from "../types/transaction";

export const transactionApi = {
    createTransferIntent: async (
        request: CreateTransferIntentRequest,
    ): Promise<
        ApiResponse<CreateTransferIntentResult>
    > => {
        const response =
            await apiClient.post<
                ApiResponse<CreateTransferIntentResult>
            >(
                API_ENDPOINTS.TRANSACTION
                    .TRANSFER_INTENTS,
                request,
            );

        return response.data;
    },

    confirmTransfer: async (
        request: ConfirmTransferRequest,
    ): Promise<ApiResponse<Transaction>> => {
        const response =
            await apiClient.post<
                ApiResponse<Transaction>
            >(
                API_ENDPOINTS.TRANSACTION
                    .CONFIRM_TRANSFER,
                request,
            );

        return response.data;
    },

    getById: async (
        transactionId: string,
    ): Promise<ApiResponse<Transaction>> => {
        const response =
            await apiClient.get<
                ApiResponse<Transaction>
            >(
                API_ENDPOINTS.TRANSACTION.BY_ID(
                    transactionId,
                ),
            );

        return response.data;
    },

    getAccountTransactions: async (
        accountId: string,
        filters: TransactionFilters = {},
    ): Promise<
        ApiResponse<PageResponse<Transaction>>
    > => {
        const response =
            await apiClient.get<
                ApiResponse<
                    PageResponse<Transaction>
                >
            >(
                API_ENDPOINTS.TRANSACTION
                    .ACCOUNT_TRANSACTIONS(
                        accountId,
                    ),
                {
                    params: filters,
                },
            );

        return response.data;
    },

    getUserTransactions: async (
        filters: TransactionFilters = {},
    ): Promise<
        ApiResponse<PageResponse<Transaction>>
    > => {
        const response =
            await apiClient.get<
                ApiResponse<PageResponse<Transaction>>
            >(
                API_ENDPOINTS.TRANSACTION.USER_TRANSACTIONS,
                {
                    params: filters,
                },
            );

        return response.data;
    },
};