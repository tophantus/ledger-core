"use client";

import {useLocale, useTranslations} from "next-intl";
import {useState} from "react";

import {Button} from "@/components/ui/button";
import {getCreditOfferStatusColor} from "@/lib/utils/credit";
import {formatMoney} from "@/lib/utils/currency";

import {useAcceptCreditOffer} from "../hooks/use-accept-credit-offer";
import {useRejectCreditOffer} from "../hooks/use-reject-credit-offer";
import type {CreditOfferInfo} from "../types/credit-offer";

interface CreditOfferProps {
    offer: CreditOfferInfo;
    onCompleted: () => Promise<void>;
}

export function CreditOffer({
                                offer,
                                onCompleted,
                            }: CreditOfferProps) {
    const t = useTranslations("credit");
    const locale = useLocale();

    const {acceptOffer} =
        useAcceptCreditOffer();

    const {rejectOffer} =
        useRejectCreditOffer();

    const [isLoading, setIsLoading] =
        useState(false);

    const handleAccept = async () => {
        setIsLoading(true);

        try {
            const response =
                await acceptOffer(
                    offer.offerId,
                );

            if (response.success) {
                await onCompleted();
            }
        } finally {
            setIsLoading(false);
        }
    };

    const handleReject = async () => {
        setIsLoading(true);

        try {
            const response =
                await rejectOffer(
                    offer.offerId,
                );

            if (response.success) {
                await onCompleted();
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <section className="space-y-6">
            <div>
                <h1 className="text-2xl font-semibold text-primary">
                    {t("offer.title")}
                </h1>

                <p className="mt-1 text-sm text-text-muted">
                    {t("offer.description")}
                </p>
            </div>

            <div
                className="
                    rounded-xl
                    border
                    border-border
                    bg-surface
                    p-5
                    shadow-sm
                "
            >
                <div>
                    <p className="text-xs font-medium text-text-muted">
                        {t(
                            "offer.approvedLimit",
                        )}
                    </p>

                    <p className="mt-1 text-2xl font-semibold tracking-tight text-text-primary">
                        {formatMoney(
                            offer.approvedLimit,
                            offer.currency,
                            locale,
                        )}
                    </p>
                </div>

                <div
                    className="
                        mt-6
                        grid
                        gap-5
                        border-t
                        border-border
                        pt-5
                        sm:grid-cols-2
                    "
                >
                    <div>
                        <p className="text-xs font-medium text-text-muted">
                            {t("offer.status")}
                        </p>

                        <div className="mt-2">
                            <span
                                className={`
                                    inline-flex
                                    w-fit
                                    items-center
                                    rounded-full
                                    px-2.5
                                    py-1
                                    text-xs
                                    font-medium
                                    ${getCreditOfferStatusColor(
                                    offer.status,
                                    "text-background",
                                )}
                                `}
                            >
                                {t(
                                    `offer.statuses.${offer.status}`,
                                )}
                            </span>
                        </div>
                    </div>

                    <div>
                        <p className="text-xs font-medium text-text-muted">
                            {t(
                                "offer.expiresAt",
                            )}
                        </p>

                        <p className="mt-2 text-sm font-medium text-text-primary">
                            {new Date(
                                offer.expiresAt,
                            ).toLocaleString(
                                locale,
                            )}
                        </p>
                    </div>
                </div>

                <div
                    className="
                        mt-6
                        flex
                        justify-end
                        gap-3
                        border-t
                        border-border
                        pt-5
                    "
                >
                    <Button
                        type="button"
                        variant="ghost"
                        disabled={isLoading}
                        onClick={
                            handleReject
                        }
                    >
                        {t(
                            "offer.actions.reject",
                        )}
                    </Button>

                    <Button
                        type="button"
                        loading={isLoading}
                        disabled={isLoading}
                        onClick={
                            handleAccept
                        }
                    >
                        {t(
                            "offer.actions.accept",
                        )}
                    </Button>
                </div>
            </div>
        </section>
    );
}