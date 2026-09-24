"use client";

import {useCallback} from "react";

import {
    providerAccountApi
} from "../api/provider-account-api";

export function useGetProviderAccounts() {
    const getProviderAccounts = useCallback(
        async (
        ) => {
            return providerAccountApi.getAccounts(
            );
        },
        [],
    );

    return {
        getProviderAccounts,
    };
}