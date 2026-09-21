"use client";

import {useCallback} from "react";

import {creditOfferApi} from "../api/credit-offer-api";

export function useAcceptCreditOffer() {
    const acceptOffer = useCallback(
        async (offerId: string) => {
            return creditOfferApi.accept(
                offerId,
            );
        },
        [],
    );

    return {
        acceptOffer,
    };
}