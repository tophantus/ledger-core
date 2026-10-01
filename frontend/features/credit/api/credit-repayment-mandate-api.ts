import {apiClient} from "@/lib/api/axios";
import {API_ENDPOINTS} from "@/lib/constants/api";

import type {
    CreateCreditRepaymentMandateRequest,
    CreateCreditRepaymentMandateResponse,
    RevokeCreditRepaymentMandateResponse,
    UpdateCreditRepaymentMandateRequest,
    UpdateCreditRepaymentMandateResponse,
} from "../types/credit-repayment-mandate";

export const creditRepaymentMandateApi = {
    create: async (
        request: CreateCreditRepaymentMandateRequest,
    ): Promise<CreateCreditRepaymentMandateResponse> => {
        const response =
            await apiClient.post<CreateCreditRepaymentMandateResponse>(
                API_ENDPOINTS.CREDIT.REPAYMENT_MANDATES.BASE,
                request,
            );

        return response.data;
    },

    update: async (
        mandateId: string,
        request: UpdateCreditRepaymentMandateRequest,
    ): Promise<UpdateCreditRepaymentMandateResponse> => {
        const response =
            await apiClient.put<UpdateCreditRepaymentMandateResponse>(
                API_ENDPOINTS.CREDIT.REPAYMENT_MANDATES.BY_ID(
                    mandateId,
                ),
                request,
            );

        return response.data;
    },

    revoke: async (
        mandateId: string,
    ): Promise<RevokeCreditRepaymentMandateResponse> => {
        const response =
            await apiClient.delete<RevokeCreditRepaymentMandateResponse>(
                API_ENDPOINTS.CREDIT.REPAYMENT_MANDATES.BY_ID(
                    mandateId,
                ),
            );

        return response.data;
    },
};