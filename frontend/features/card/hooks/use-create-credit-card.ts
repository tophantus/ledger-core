"use client";

import {useCallback} from "react";

import {cardApi} from "../api/card-api";
import type {
    CreateCreditCardRequest,
} from "../types/card";

export function useCreateCreditCard() {
    const createCreditCard =
        useCallback(
            async (
                request: CreateCreditCardRequest,
            ) =>
                cardApi.createCredit(request),
            [],
        );

    return {
        createCreditCard,
    };
}