"use client";

import {useCallback} from "react";

import {cardApi} from "../api/card-api";
import type {
    CreateDebitCardRequest,
} from "../types/card";

export function useCreateDebitCard() {
    const createDebitCard =
        useCallback(
            async (
                request: CreateDebitCardRequest,
            ) =>
                cardApi.createDebit(request),
            [],
        );

    return {
        createDebitCard,
    };
}