"use client";

import {useCallback} from "react";

import {adminInterestApi} from "../api/admin-interest-api";
import type {
    InterestAccrualFilters,
} from "../types/admin-interest";

export function useAdminInterestAccruals() {
    const getAccruals = useCallback(
        async (
            filters: InterestAccrualFilters = {},
        ) => {
            return adminInterestApi.getAccruals(
                filters,
            );
        },
        [],
    );

    return {
        getAccruals,
    };
}