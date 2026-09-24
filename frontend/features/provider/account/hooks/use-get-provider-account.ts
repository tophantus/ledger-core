"use client";

import {useCallback} from "react";

import {
    providerAccountApi,
    type ProviderCredentials,
} from "../api/provider-account-api";

export function useGetProviderAccounts() {
    const getProviderAccounts = useCallback(
        async (
            credentials: ProviderCredentials,
        ) => {
            return providerAccountApi.getAccounts(
                credentials,
            );
        },
        [],
    );

    return {
        getProviderAccounts,
    };
}