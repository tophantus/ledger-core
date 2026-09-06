"use client";

import {accountApi} from "../api/account-api";

export function useAccountHolder() {
    const getAccountHolder = async (
        accountNo: string,
    ) => {
        return accountApi.getHolder(accountNo);
    };

    return {
        getAccountHolder,
    };
}