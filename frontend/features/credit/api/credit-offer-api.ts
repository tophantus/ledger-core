import {apiClient} from "@/lib/api/axios";
import {API_ENDPOINTS} from "@/lib/constants/api";
import type {ApiResponse} from "@/lib/api/types";

import type {
    AcceptCreditOfferResult,
    CreditOfferInfo,
    RejectCreditOfferResult,
} from "../types/credit-offer";

export const creditOfferApi = {
    getLatest: async (): Promise<
        ApiResponse<CreditOfferInfo>
    > => {
        const response =
            await apiClient.get<
                ApiResponse<CreditOfferInfo>
            >(
                API_ENDPOINTS.CREDIT.OFFERS.LATEST,
            );

        return response.data;
    },

    accept: async (
        offerId: string,
    ): Promise<
        ApiResponse<AcceptCreditOfferResult>
    > => {
        const response =
            await apiClient.post<
                ApiResponse<AcceptCreditOfferResult>
            >(
                API_ENDPOINTS.CREDIT.OFFERS.ACCEPT(
                    offerId,
                ),
            );

        return response.data;
    },

    reject: async (
        offerId: string,
    ): Promise<
        ApiResponse<RejectCreditOfferResult>
    > => {
        const response =
            await apiClient.post<
                ApiResponse<RejectCreditOfferResult>
            >(
                API_ENDPOINTS.CREDIT.OFFERS.REJECT(
                    offerId,
                ),
            );

        return response.data;
    },
};