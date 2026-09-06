"use client";

import {useCallback} from "react";

import {transactionApi} from "../api/transaction-api";
import type {TransactionFilters} from "../types/transaction";

export function useUserTransactions() {
    const getUserTransactions = useCallback(
        async (
            filters: TransactionFilters = {},
        ) => {
            return transactionApi.getUserTransactions(
                filters,
            );
        },
        [],
    );

    return {
        getUserTransactions,
    };
}