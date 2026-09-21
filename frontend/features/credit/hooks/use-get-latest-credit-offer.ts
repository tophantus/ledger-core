"use client";

import {useCallback} from "react";

import {creditOfferApi} from "../api/credit-offer-api";

export function useGetLatestCreditOffer() {
    const getLatestOffer = useCallback(
        async () => {
            return creditOfferApi.getLatest();
        },
        [],
    );

    return {
        getLatestOffer,
    };
}