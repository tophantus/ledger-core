import {apiClient} from "@/lib/api/axios";
import type {ApiResponse, PageResponse} from "@/lib/api/types";
import {API_ENDPOINTS} from "@/lib/constants/api";

import type {
    AdminAccount, AdminAccountDetail,
    AdminAccountFilters
} from "../types/admin-account";

export const adminAccountApi = {
    getAccounts: async (
        filters?: AdminAccountFilters,
    ): Promise<
        ApiResponse<PageResponse<AdminAccount>>
    > => {
        const response =
            await apiClient.get<
                ApiResponse<
                    PageResponse<AdminAccount>
                >
            >(
                API_ENDPOINTS.ADMIN.ACCOUNT.BASE,
                {
                    params: filters,
                },
            );

        return response.data;
    },
    getAccountDetail: async (
        accountId: string,
    ): Promise<ApiResponse<AdminAccountDetail>> => {
        const response =
            await apiClient.get<
                ApiResponse<AdminAccountDetail>
            >(
                API_ENDPOINTS.ADMIN.ACCOUNT.BY_ID(
                    accountId,
                ),
            );

        return response.data;
    },
};