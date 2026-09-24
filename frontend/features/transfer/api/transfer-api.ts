import {apiClient} from "@/lib/api/axios";
import {API_ENDPOINTS} from "@/lib/constants/api";
import type {ApiResponse} from "@/lib/api/types";
import {
    Transaction
} from "@/features/transaction/types/transaction";
import {
    ConfirmTransferRequest,
    CreateTransferIntentRequest,
    CreateTransferIntentResult
} from "@/features/transfer/types/transfer";

export const transferApi = {
    createTransferIntent: async (
        request: CreateTransferIntentRequest,
    ): Promise<
        ApiResponse<CreateTransferIntentResult>
    > => {
        const response =
            await apiClient.post<
                ApiResponse<CreateTransferIntentResult>
            >(
                API_ENDPOINTS.TRANSFER.CREATE_INTENT,
                request,
            );

        return response.data;
    },

    confirmTransfer: async (
        request: ConfirmTransferRequest,
    ): Promise<ApiResponse<Transaction>> => {
        const response =
            await apiClient.post<
                ApiResponse<Transaction>
            >(
                API_ENDPOINTS.TRANSFER.CONFIRM_INTENT,
                request,
            );

        return response.data;
    }
};