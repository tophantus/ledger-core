import {apiClient} from "@/lib/api/axios";
import {API_ENDPOINTS} from "@/lib/constants/api";

import type {
    GetActiveProductsResponse,
} from "../types/product";

export const productApi = {
    getActiveProducts:
        async (): Promise<GetActiveProductsResponse> => {
            const response =
                await apiClient.get<GetActiveProductsResponse>(
                    API_ENDPOINTS.PRODUCT.BASE,
                );

            return response.data;
        },
};