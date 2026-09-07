import {apiClient} from "@/lib/api/axios";
import type {ApiResponse} from "@/lib/api/types";
import {API_ENDPOINTS} from "@/lib/constants/api";

import type {
    DepositMoneyRequest,
} from "../types/admin-transaction";
import type {Transaction} from "@/features/transaction/types/transaction";

export const adminTransactionApi = {
    deposit: async (
        request: DepositMoneyRequest,
    ): Promise<ApiResponse<Transaction>> => {
        const response =
            await apiClient.post<
                ApiResponse<Transaction>
            >(
                API_ENDPOINTS.ADMIN.TRANSACTION
                    .DEPOSIT,
                request,
            );

        return response.data;
    },
};