import {apiClient} from "@/lib/api/axios";
import {API_ENDPOINTS} from "@/lib/constants/api";

import type {
    ExecuteWithdrawalApiResponse,
    ExecuteWithdrawalRequest,
} from "../types/atm";

const ATM_TERMINAL_CODE =
    process.env.NEXT_PUBLIC_ATM_TERMINAL_CODE;

const ATM_CREDENTIAL =
    process.env.NEXT_PUBLIC_ATM_CREDENTIAL;

export const atmApi = {
    executeWithdrawal: async (
        request: ExecuteWithdrawalRequest,
    ): Promise<ExecuteWithdrawalApiResponse> => {
        const response =
            await apiClient.post<ExecuteWithdrawalApiResponse>(
                API_ENDPOINTS.ATM.EXECUTE_WITHDRAWAL,
                request,
                {
                    headers: {
                        "X-ATM-Terminal":
                        ATM_TERMINAL_CODE,
                        "X-ATM-Credential":
                        ATM_CREDENTIAL,
                    },
                },
            );

        return response.data;
    },
};