"use client";

import {withdrawalApi} from "../api/withdrawal-api";
import type {
    CreateWithdrawalRequest,
} from "../types/withdrawal";

export function useCreateWithdrawalRequest() {
    const createWithdrawalRequest = async (
        request: CreateWithdrawalRequest,
    ) => {
        return withdrawalApi.createRequest(
            request,
        );
    };

    return {
        createWithdrawalRequest,
    };
}