"use client";

import {ConfirmTransferRequest} from "@/features/transfer/types/transfer";
import {transferApi} from "@/features/transfer/api/transfer-api";

export function useConfirmTransfer() {
    const confirmTransfer = async (
        request: ConfirmTransferRequest,
    ) => {
        return transferApi.confirmTransfer(
            request,
        );
    };

    return {
        confirmTransfer,
    };
}