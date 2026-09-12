"use client";

import {useCallback} from "react";

import {adminInterestApi} from "../api/admin-interest-api";
import type {
    InterestPostingFilters,
} from "../types/admin-interest";

export function useAdminInterestPostings() {
    const getPostings = useCallback(
        async (
            filters: InterestPostingFilters = {},
        ) => {
            return adminInterestApi.getPostings(
                filters,
            );
        },
        [],
    );

    return {
        getPostings,
    };
}