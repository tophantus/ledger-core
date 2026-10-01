import {apiClient} from "@/lib/api/axios";
import {API_ENDPOINTS} from "@/lib/constants/api";

import type {
    GetCreditStatementsResponse,
} from "../types/credit-statement";

export const creditStatementApi = {
    get: async (
        creditFacilityId: string,
        page = 0,
        size = 20,
    ): Promise<GetCreditStatementsResponse> => {
        const response =
            await apiClient.get<GetCreditStatementsResponse>(
                API_ENDPOINTS.CREDIT.FACILITY_STATEMENTS(
                    creditFacilityId,
                ),
                {
                    params: {
                        page,
                        size,
                    },
                },
            );

        return response.data;
    },
};