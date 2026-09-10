"use client";

import {atmApi} from "../api/atm-api";

import type {
    ExecuteWithdrawalRequest,
} from "../types/atm";

export function useExecuteWithdrawal() {
    const executeWithdrawal = async (
        request: ExecuteWithdrawalRequest,
    ) => {
        return atmApi.executeWithdrawal(request);
    };

    return {
        executeWithdrawal,
    };
}