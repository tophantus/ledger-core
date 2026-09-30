"use client";

import {useCallback} from "react";

import {creditRepaymentMandateApi} from "../api/credit-repayment-mandate-api";
import type {
    UpdateCreditRepaymentMandateRequest,
} from "../types/credit-repayment-mandate";

export function useUpdateCreditRepaymentMandate() {
    const updateMandate = useCallback(
        async (
            mandateId: string,
            request: UpdateCreditRepaymentMandateRequest,
        ) => {
            return creditRepaymentMandateApi.update(
                mandateId,
                request,
            );
        },
        [],
    );

    return {
        updateMandate,
    };
}