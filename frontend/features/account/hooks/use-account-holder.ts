"use client";

import {useCallback} from "react";

import {accountApi} from "../api/account-api";

export function useAccountHolder() {
    const getAccountHolder = useCallback(
        async (accountNo: string) => {
            return accountApi.getHolder(accountNo);
        },
        [],
    );

    return {
        getAccountHolder,
    };
}