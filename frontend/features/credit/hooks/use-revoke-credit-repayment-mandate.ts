"use client";

import {useCallback} from "react";

import {creditRepaymentMandateApi} from "../api/credit-repayment-mandate-api";

export function useRevokeCreditRepaymentMandate() {
    const revokeMandate = useCallback(
        async (mandateId: string) => {
            return creditRepaymentMandateApi.revoke(
                mandateId,
            );
        },
        [],
    );

    return {
        revokeMandate,
    };
}