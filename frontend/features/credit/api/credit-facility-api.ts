import {apiClient} from "@/lib/api/axios";
import {API_ENDPOINTS} from "@/lib/constants/api";
import type {ApiResponse} from "@/lib/api/types";

import type {GetUserCreditFacilityResult} from "../types/credit-facility";

export const creditFacilityApi = {
    get: async (): Promise<
        ApiResponse<GetUserCreditFacilityResult>
    > => {
        const response =
            await apiClient.get<
                ApiResponse<GetUserCreditFacilityResult>
            >(
                API_ENDPOINTS.CREDIT.FACILITY,
            );

        return response.data;
    },
};