"use client";

import {useCallback} from "react";

import {
    providerAccountApi
} from "../api/provider-account-api";

export function useGetProviderAccount() {
    const getProviderAccount = useCallback(
        async (
            accountId: string
        ) => {
            return providerAccountApi.getById(
                accountId
            );
        },
        [],
    );

    return {
        getProviderAccount,
    };
}