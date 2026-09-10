import {apiClient} from "@/lib/api/axios";
import {API_ENDPOINTS} from "@/lib/constants/api";
import type {
    ApiResponse,
    PageResponse,
} from "@/lib/api/types";

import type {
    ConfirmWithdrawalRequest,
    ConfirmWithdrawalRequestResponse,
    CreateWithdrawalRequest,
    WithdrawalIntent,
    WithdrawalIntentFilters,
    WithdrawalRequestResponse,
} from "../types/withdrawal";

export const withdrawalApi = {
    createRequest: async (
        request: CreateWithdrawalRequest,
    ): Promise<
        ApiResponse<WithdrawalRequestResponse>
    > => {
        const response =
            await apiClient.post<
                ApiResponse<WithdrawalRequestResponse>
            >(
                API_ENDPOINTS.WITHDRAWAL.REQUESTS,
                request,
            );

        return response.data;
    },

    confirmRequest: async (
        requestId: string,
        request: ConfirmWithdrawalRequest,
    ): Promise<
        ApiResponse<ConfirmWithdrawalRequestResponse>
    > => {
        const response =
            await apiClient.post<
                ApiResponse<ConfirmWithdrawalRequestResponse>
            >(
                API_ENDPOINTS.WITHDRAWAL.CONFIRM(
                    requestId,
                ),
                request,
            );

        return response.data;
    },

    getIntents: async (
        filters: WithdrawalIntentFilters = {},
    ): Promise<
        ApiResponse<PageResponse<WithdrawalIntent>>
    > => {
        const response =
            await apiClient.get<
                ApiResponse<
                    PageResponse<WithdrawalIntent>
                >
            >(
                API_ENDPOINTS.WITHDRAWAL.INTENTS,
                {
                    params: filters,
                },
            );

        return response.data;
    },
};