"use client";

import {useCallback} from "react";

import {adminAccountApi} from "../api/admin-account-api";
import type {AdminAccountFilters} from "../types/admin-account";

export function useAdminAccounts() {
    const getAdminAccounts = useCallback(
        async (
            filters?: AdminAccountFilters,
        ) => {
            return adminAccountApi.getAccounts(
                filters,
            );
        },
        [],
    );

    return {
        getAdminAccounts,
    };
}