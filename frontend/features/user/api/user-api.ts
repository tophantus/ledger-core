import {apiClient} from "@/lib/api/axios";
import {API_ENDPOINTS} from "@/lib/constants/api";
import type {ApiResponse} from "@/lib/api/types";

import type {CurrentUser} from "../types/user";

export const userApi = {
    getCurrentUser: async (): Promise<
        ApiResponse<CurrentUser>
    > => {
        const response = await apiClient.get<
            ApiResponse<CurrentUser>
        >(
            API_ENDPOINTS.USER.CURRENT_USER,
        );

        return response.data;
    },
};