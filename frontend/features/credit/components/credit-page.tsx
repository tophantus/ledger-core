"use client";

import {
    useCallback,
    useEffect,
    useState,
} from "react";
import {useTranslations} from "next-intl";

import {useGetCreditFacility} from "../hooks/use-get-credit-facility";
import {useGetLatestCreditOffer} from "../hooks/use-get-latest-credit-offer";
import type {GetUserCreditFacilityResult} from "../types/credit-facility";
import type {CreditOfferInfo} from "../types/credit-offer";

import {CreditFacility} from "./credit-facility";
import {CreditOffer} from "./credit-offer";

export function CreditContentPage() {
    const t = useTranslations("credit");

    const {getLatestOffer} =
        useGetLatestCreditOffer();

    const {getFacility} =
        useGetCreditFacility();

    const [offer, setOffer] =
        useState<CreditOfferInfo | null>(null);

    const [facility, setFacility] =
        useState<GetUserCreditFacilityResult | null>(
            null,
        );

    const [isLoading, setIsLoading] =
        useState(true);

    const [hasError, setHasError] =
        useState(false);

    const loadCredit = useCallback(
        async (showLoading = false) => {
            if (showLoading) {
                setIsLoading(true);
                setHasError(false);
            }

            try {
                const offerResponse =
                    await getLatestOffer();

                if (
                    offerResponse.success &&
                    offerResponse.data
                ) {
                    setOffer(
                        offerResponse.data,
                    );
                    setFacility(null);
                    return;
                }

                const facilityResponse =
                    await getFacility();

                if (
                    !facilityResponse.success
                ) {
                    setHasError(true);
                    return;
                }

                setOffer(null);
                setFacility(
                    facilityResponse.data,
                );
            } catch {
                setHasError(true);
            } finally {
                setIsLoading(false);
            }
        },
        [
            getLatestOffer,
            getFacility,
        ],
    );

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        void loadCredit();
    }, [loadCredit]);

    if (isLoading) {
        return (
            <div className="space-y-4">
                <div className="h-8 w-32 animate-pulse rounded bg-background-subtle" />

                <div className="h-48 animate-pulse rounded-lg bg-background-subtle" />
            </div>
        );
    }

    if (hasError) {
        return (
            <div
                className="
                    rounded-lg
                    border
                    border-border
                    bg-surface
                    p-6
                "
            >
                <p className="text-sm text-danger">
                    {t("errors.loadFailed")}
                </p>
            </div>
        );
    }

    if (offer) {
        return (
            <CreditOffer
                offer={offer}
                onCompleted={() =>
                    loadCredit(true)
                }
            />
        );
    }

    return (
        <CreditFacility
            facility={facility}
        />
    );
}