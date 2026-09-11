import {apiClient} from "@/lib/api/axios";
import type {ApiResponse} from "@/lib/api/types";
import {API_ENDPOINTS} from "@/lib/constants/api";
import {CurrentBusinessDay} from "@/features/admin/business-day/types/admin-business-day";

export const adminBusinessDayApi = {
    getCurrentBusinessDay: async (): Promise<
        ApiResponse<CurrentBusinessDay>
    > => {
        const response =
            await apiClient.get<
                ApiResponse<CurrentBusinessDay>
            >(
                API_ENDPOINTS.ADMIN.BUSINESS_DAY.CURRENT,
            );

        return response.data;
    },

    closeBusinessDay: async (): Promise<
        ApiResponse<void>
    > => {
        const response =
            await apiClient.post<ApiResponse<void>>(
                API_ENDPOINTS.ADMIN.BUSINESS_DAY.CLOSE,
            );

        return response.data;
    },
};