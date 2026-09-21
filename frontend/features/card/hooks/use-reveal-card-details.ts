"use client";

import {useCallback} from "react";

import {cardApi} from "../api/card-api";

export function useRevealCardDetails() {
    const revealCardDetails =
        useCallback(
            async (
                cardId: string,
                pin: string,
            ) =>
                cardApi.reveal(
                    cardId,
                    pin,
                ),
            [],
        );

    return {
        revealCardDetails,
    };
}