"use client";

import {useCallback} from "react";

import {adminTransactionApi} from "../api/admin-transaction-api";
import type {
    DepositMoneyRequest,
} from "../types/admin-transaction";

export function useAdminTransactions() {
    const deposit = useCallback(
        async (
            request: DepositMoneyRequest,
        ) => {
            return adminTransactionApi.deposit(
                request,
            );
        },
        [],
    );

    return {
        deposit,
    };
}