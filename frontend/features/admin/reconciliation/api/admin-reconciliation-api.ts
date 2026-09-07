import {apiClient} from "@/lib/api/axios";
import type {
    ApiResponse,
    PageResponse,
} from "@/lib/api/types";
import {API_ENDPOINTS} from "@/lib/constants/api";

import type {
    ReconciliationException,
    ReconciliationExceptionFilters,
    ReconciliationSummary,
} from "../types/admin-reconciliation";

export const adminReconciliationApi = {
    getSummary: async (
        businessDate?: string,
    ): Promise<ApiResponse<ReconciliationSummary>> => {
        const response =
            await apiClient.get<
                ApiResponse<ReconciliationSummary>
            >(
                API_ENDPOINTS.ADMIN.RECONCILIATION.SUMMARY,
                {
                    params: businessDate
                        ? {businessDate}
                        : undefined,
                },
            );

        return response.data;
    },

    getExceptions: async (
        filters?: ReconciliationExceptionFilters,
    ): Promise<
        ApiResponse<
            PageResponse<ReconciliationException>
        >
    > => {
        const response =
            await apiClient.get<
                ApiResponse<
                    PageResponse<ReconciliationException>
                >
            >(
                API_ENDPOINTS.ADMIN.RECONCILIATION.EXCEPTIONS,
                {
                    params: filters,
                },
            );

        return response.data;
    },
};