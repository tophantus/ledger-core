"use client";

import {useCallback} from "react";

import {adminInterestApi} from "../api/admin-interest-api";
import type {
    InterestRunFilters,
} from "../types/admin-interest";

export function useAdminInterestRuns() {
    const getRuns = useCallback(
        async (
            filters: InterestRunFilters = {},
        ) => {
            return adminInterestApi.getRuns(
                filters,
            );
        },
        [],
    );

    return {
        getRuns,
    };
}