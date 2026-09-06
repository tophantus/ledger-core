"use client";

import {transactionApi} from "../api/transaction-api";
import type {
    CreateTransferIntentRequest,
} from "../types/transaction";

export function useCreateTransferIntent() {
    const createTransferIntent = async (
        request: CreateTransferIntentRequest,
    ) => {
        return transactionApi
            .createTransferIntent(request);
    };

    return {
        createTransferIntent,
    };
}