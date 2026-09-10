"use client";

import {useCallback} from "react";

import {withdrawalApi} from "../api/withdrawal-api";
import type {
    WithdrawalIntentFilters,
} from "../types/withdrawal";

export function useWithdrawalIntents() {
    const getWithdrawalIntents = useCallback(
        async (
            filters: WithdrawalIntentFilters = {},
        ) => {
            return withdrawalApi.getIntents(
                filters,
            );
        },
        [],
    );

    return {
        getWithdrawalIntents,
    };
}