"use client";

import {useCallback} from "react";

import {transactionApi} from "../api/transaction-api";
import type {TransactionFilters} from "../types/transaction";

export function useTransactions() {
    const getTransactions = useCallback(
        async (
            filters: TransactionFilters = {},
        ) => {
            return transactionApi.getTransactions(
                filters,
            );
        },
        [],
    );

    return {
        getTransactions,
    };
}