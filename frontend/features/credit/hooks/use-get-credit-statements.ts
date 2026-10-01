"use client";

import {useCallback} from "react";

import {creditStatementApi} from "../api/credit-statement-api";

export function useGetCreditStatements() {
    const getStatements = useCallback(
        async (
            creditFacilityId: string,
            page = 0,
            size = 20,
        ) => {
            return creditStatementApi.get(
                creditFacilityId,
                page,
                size,
            );
        },
        [],
    );

    return {
        getStatements,
    };
}