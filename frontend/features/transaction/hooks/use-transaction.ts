"use client";

import {useCallback} from "react";

import {transactionApi} from "../api/transaction-api";

export function useTransaction() {
    const getTransaction = useCallback(
        async (
            transactionId: string,
        ) => {
            return transactionApi.getById(
                transactionId,
            );
        },
        [],
    );

    return {
        getTransaction,
    };
}