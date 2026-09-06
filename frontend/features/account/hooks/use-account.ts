"use client";

import {useCallback} from "react";

import {accountApi} from "../api/account-api";

export function useAccount() {
    const getAccount = useCallback(
        async (accountId: string) => {
            return accountApi.getById(accountId);
        },
        [],
    );

    return {
        getAccount,
    };
}