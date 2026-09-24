import {apiClient} from "@/lib/api/axios";
import {API_ENDPOINTS} from "@/lib/constants/api";

import type {
    RegisterPaymentProviderRequest,
    RegisterPaymentProviderResponse,
} from "../types/payment-provider";

export const paymentProviderApi = {
    register: async (
        request: RegisterPaymentProviderRequest,
    ): Promise<RegisterPaymentProviderResponse> => {
        const response =
            await apiClient.post<
                RegisterPaymentProviderResponse
            >(
                API_ENDPOINTS.PROVIDER.BASE,
                request,
            );

        return response.data;
    },
};