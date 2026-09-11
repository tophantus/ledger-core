"use client";

import {useCallback} from "react";

import {adminBusinessDayApi} from "../api/admin-business-day-api";

export function useAdminBusinessDay() {
    const getCurrentBusinessDay =
        useCallback(
            async () => {
                return adminBusinessDayApi
                    .getCurrentBusinessDay();
            },
            [],
        );

    const closeBusinessDay =
        useCallback(
            async () => {
                return adminBusinessDayApi
                    .closeBusinessDay();
            },
            [],
        );

    return {
        getCurrentBusinessDay,
        closeBusinessDay,
    };
}