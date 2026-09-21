"use client";

import {useCallback} from "react";

import {cardApi} from "../api/card-api";

export function useGetCardById() {
    const getCardById = useCallback(
        async (cardId: string) =>
            cardApi.getById(cardId),
        [],
    );

    return {
        getCardById,
    };
}