"use client";

import {useCallback} from "react";

import {adminBusinessDayApi} from "../api/admin-business-day-api";

export function useAdminBusinessDay() {
    const closeBusinessDay = useCallback(
        async () => {
            return adminBusinessDayApi.closeBusinessDay();
        },
        [],
    );

    return {
        closeBusinessDay,
    };
}