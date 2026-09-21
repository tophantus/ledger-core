"use client";

import {useCallback} from "react";

import {creditFacilityApi} from "../api/credit-facility-api";

export function useGetCreditFacility() {
    const getFacility = useCallback(
        async () => {
            return creditFacilityApi.get();
        },
        [],
    );

    return {
        getFacility,
    };
}