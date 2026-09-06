"use client";

import {accountApi} from "../api/account-api";

export function useAccountByAccountNo() {
    const getAccountByAccountNo = async (
        accountNo: string,
    ) => {
        return accountApi.getByAccountNo(
            accountNo,
        );
    };

    return {
        getAccountByAccountNo,
    };
}