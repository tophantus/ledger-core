import {apiClient} from "@/lib/api/axios";
import {API_ENDPOINTS} from "@/lib/constants/api";
import type {ApiResponse, PageResponse} from "@/lib/api/types";

import type {
    CardInfo,
    CreateCreditCardRequest,
    CreateCreditCardResult,
    CreateDebitCardRequest,
    CreateDebitCardResult,
    RevealedCardDetails,
} from "../types/card";

export const cardApi = {
    createDebit: async (
        request: CreateDebitCardRequest,
    ): Promise<
        ApiResponse<CreateDebitCardResult>
    > => {
        const response =
            await apiClient.post<
                ApiResponse<CreateDebitCardResult>
            >(
                API_ENDPOINTS.CARD.DEBIT,
                request,
            );

        return response.data;
    },

    createCredit: async (
        request: CreateCreditCardRequest,
    ): Promise<
        ApiResponse<CreateCreditCardResult>
    > => {
        const response =
            await apiClient.post<
                ApiResponse<CreateCreditCardResult>
            >(
                API_ENDPOINTS.CARD.CREDIT,
                request,
            );

        return response.data;
    },

    getAll: async (
        page = 0,
        size = 20,
    ): Promise<
        ApiResponse<PageResponse<CardInfo>>
    > => {
        const response =
            await apiClient.get<
                ApiResponse<PageResponse<CardInfo>>
            >(
                API_ENDPOINTS.CARD.BASE,
                {
                    params: {
                        page,
                        size,
                    },
                },
            );

        return response.data;
    },

    getById: async (
        cardId: string,
    ): Promise<ApiResponse<CardInfo>> => {
        const response =
            await apiClient.get<
                ApiResponse<CardInfo>
            >(
                API_ENDPOINTS.CARD.BY_ID(cardId),
            );

        return response.data;
    },

    reveal: async (
        cardId: string,
        pin: string,
    ): Promise<
        ApiResponse<RevealedCardDetails>
    > => {
        const response =
            await apiClient.post<
                ApiResponse<RevealedCardDetails>
            >(
                API_ENDPOINTS.CARD.REVEAL(cardId),
                {pin},
            );

        return response.data;
    },
};