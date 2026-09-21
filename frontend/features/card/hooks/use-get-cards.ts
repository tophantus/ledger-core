"use client";

import {useCallback} from "react";

import {cardApi} from "../api/card-api";

export function useGetCards() {
    const getCards = useCallback(
        async (
            page = 0,
            size = 20,
        ) =>
            cardApi.getAll(page, size),
        [],
    );

    return {
        getCards,
    };
}