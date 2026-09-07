"use client";

import {useCallback} from "react";

import {adminReconciliationApi} from "../api/admin-reconciliation-api";
import type {
    ReconciliationExceptionFilters,
} from "../types/admin-reconciliation";

export function useAdminReconciliation() {
    const getSummary = useCallback(
        async (businessDate?: string) => {
            return adminReconciliationApi.getSummary(
                businessDate,
            );
        },
        [],
    );

    const getExceptions = useCallback(
        async (
            filters?: ReconciliationExceptionFilters,
        ) => {
            return adminReconciliationApi.getExceptions(
                filters,
            );
        },
        [],
    );

    return {
        getSummary,
        getExceptions,
    };
}