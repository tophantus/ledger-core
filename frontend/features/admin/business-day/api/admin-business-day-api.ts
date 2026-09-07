import {apiClient} from "@/lib/api/axios";
import type {ApiResponse} from "@/lib/api/types";
import {API_ENDPOINTS} from "@/lib/constants/api";

export const adminBusinessDayApi = {
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