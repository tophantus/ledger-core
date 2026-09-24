"use client";

import {CreateTransferIntentRequest} from "@/features/transfer/types/transfer";
import {transferApi} from "@/features/transfer/api/transfer-api";

export function useCreateTransferIntent() {
    const createTransferIntent = async (
        request: CreateTransferIntentRequest,
    ) => {
        return transferApi
            .createTransferIntent(request);
    };

    return {
        createTransferIntent,
    };
}