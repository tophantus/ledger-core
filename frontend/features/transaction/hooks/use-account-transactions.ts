"use client";

import {useCallback} from "react";

import {transactionApi} from "../api/transaction-api";
import type {
    TransactionFilters,
} from "../types/transaction";

export function useAccountTransactions() {
    const getAccountTransactions =
        useCallback(
            async (
                accountId: string,
                filters: TransactionFilters = {},
            ) => {
                return transactionApi
                    .getAccountTransactions(
                        accountId,
                        filters,
                    );
            },
            [],
        );

    return {
        getAccountTransactions,
    };
}