"use client";

import {useCallback} from "react";

import {
    providerAccountApi,
    type ProviderCredentials,
} from "../api/provider-account-api";

export function useGetProviderAccount() {
    const getProviderAccount = useCallback(
        async (
            accountId: string,
            credentials: ProviderCredentials,
        ) => {
            return providerAccountApi.getById(
                accountId,
                credentials,
            );
        },
        [],
    );

    return {
        getProviderAccount,
    };
}