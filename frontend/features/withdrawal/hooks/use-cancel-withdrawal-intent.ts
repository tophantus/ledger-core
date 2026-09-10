"use client";

import {withdrawalApi} from "../api/withdrawal-api";

export function useCancelWithdrawalIntent() {
    const cancelWithdrawalIntent = async (
        intentId: string,
    ) => {
        return withdrawalApi.cancelIntent(
            intentId,
        );
    };

    return {
        cancelWithdrawalIntent,
    };
}