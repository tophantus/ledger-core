import {apiClient} from "@/lib/api/axios";
import {API_ENDPOINTS} from "@/lib/constants/api";

import type {
    GetProviderAccountResponse,
    GetProviderAccountsResponse,
} from "../types/provider-account";

const PROVIDER_CLIENT_ID =
    process.env.NEXT_PUBLIC_PROVIDER_CLIENT_ID ?? "";

const PROVIDER_CREDENTIAL =
    process.env.NEXT_PUBLIC_PROVIDER_CREDENTIAL ?? "";

const providerHeaders = {
    "X-Provider-Client-Id":
    PROVIDER_CLIENT_ID,
    "X-Provider-Credential":
    PROVIDER_CREDENTIAL,
};

export const providerAccountApi = {
    getAccounts: async (): Promise<
        GetProviderAccountsResponse
    > => {
        const response =
            await apiClient.get<
                GetProviderAccountsResponse
            >(
                API_ENDPOINTS.PROVIDER.ACCOUNTS.BASE,
                {
                    headers: providerHeaders,
                },
            );

        return response.data;
    },

    getById: async (
        accountId: string,
    ): Promise<GetProviderAccountResponse> => {
        const response =
            await apiClient.get<
                GetProviderAccountResponse
            >(
                API_ENDPOINTS.PROVIDER.ACCOUNTS.BY_ID(
                    accountId,
                ),
                {
                    headers: providerHeaders,
                },
            );

        return response.data;
    },
};