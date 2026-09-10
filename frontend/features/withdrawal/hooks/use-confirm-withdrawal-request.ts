"use client";

import {withdrawalApi} from "../api/withdrawal-api";
import type {
    ConfirmWithdrawalRequest,
} from "../types/withdrawal";

export function useConfirmWithdrawalRequest() {
    const confirmWithdrawalRequest = async (
        requestId: string,
        request: ConfirmWithdrawalRequest,
    ) => {
        return withdrawalApi.confirmRequest(
            requestId,
            request,
        );
    };

    return {
        confirmWithdrawalRequest,
    };
}