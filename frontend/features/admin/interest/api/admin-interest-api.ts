import {apiClient} from "@/lib/api/axios";
import type {
    ApiResponse,
    PageResponse,
} from "@/lib/api/types";
import {API_ENDPOINTS} from "@/lib/constants/api";

import type {
    InterestAccrual,
    InterestAccrualFilters,
    InterestPosting,
    InterestPostingFilters,
    InterestRun,
    InterestRunFilters,
} from "../types/admin-interest";

export const adminInterestApi = {
    getRuns: async (
        filters: InterestRunFilters = {},
    ): Promise<
        ApiResponse<PageResponse<InterestRun>>
    > => {
        const response =
            await apiClient.get<
                ApiResponse<PageResponse<InterestRun>>
            >(
                API_ENDPOINTS.ADMIN.INTEREST.RUNS,
                {
                    params: filters,
                },
            );

        return response.data;
    },

    getAccruals: async (
        filters: InterestAccrualFilters = {},
    ): Promise<
        ApiResponse<PageResponse<InterestAccrual>>
    > => {
        const response =
            await apiClient.get<
                ApiResponse<PageResponse<InterestAccrual>>
            >(
                API_ENDPOINTS.ADMIN.INTEREST.ACCRUALS,
                {
                    params: filters,
                },
            );

        return response.data;
    },

    getPostings: async (
        filters: InterestPostingFilters = {},
    ): Promise<
        ApiResponse<PageResponse<InterestPosting>>
    > => {
        const response =
            await apiClient.get<
                ApiResponse<PageResponse<InterestPosting>>
            >(
                API_ENDPOINTS.ADMIN.INTEREST.POSTINGS,
                {
                    params: filters,
                },
            );

        return response.data;
    },
};