"use client";

import {useCallback} from "react";

import {creditRepaymentMandateApi} from "../api/credit-repayment-mandate-api";
import type {
    CreateCreditRepaymentMandateRequest,
} from "../types/credit-repayment-mandate";

export function useCreateCreditRepaymentMandate() {
    const createMandate = useCallback(
        async (
            request: CreateCreditRepaymentMandateRequest,
        ) => {
            return creditRepaymentMandateApi.create(
                request,
            );
        },
        [],
    );

    return {
        createMandate,
    };
}