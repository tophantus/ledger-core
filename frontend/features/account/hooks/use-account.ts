"use client";

import {accountApi} from "../api/account-api";

export function useAccount() {
    const getAccount = async (
        accountId: string,
    ) => {
        return accountApi.getById(accountId);
    };

    return {
        getAccount,
    };
}