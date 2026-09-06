"use client";

import {transactionApi} from "../api/transaction-api";
import type {
    ConfirmTransferRequest,
} from "../types/transaction";

export function useConfirmTransfer() {
    const confirmTransfer = async (
        request: ConfirmTransferRequest,
    ) => {
        return transactionApi.confirmTransfer(
            request,
        );
    };

    return {
        confirmTransfer,
    };
}