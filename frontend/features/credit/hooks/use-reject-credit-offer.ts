"use client";

import {useCallback} from "react";

import {creditOfferApi} from "../api/credit-offer-api";

export function useRejectCreditOffer() {
    const rejectOffer = useCallback(
        async (offerId: string) => {
            return creditOfferApi.reject(
                offerId,
            );
        },
        [],
    );

    return {
        rejectOffer,
    };
}