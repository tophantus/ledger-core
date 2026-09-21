"use client";

import {CreditCard, Landmark} from "lucide-react";
import {useTranslations} from "next-intl";

import type {
    CardInfo,
    RevealedCardDetails,
} from "../types/card";
import {CardType} from "../types/card";

interface CardDetailsItemProps {
    card: CardInfo;
    details: RevealedCardDetails;
}

export function CardDetailsItem({
                                    card,
                                    details,
                                }: CardDetailsItemProps) {
    const t = useTranslations("card");

    const formattedPan =
        details.pan.match(/.{1,4}/g)?.join(" ") ??
        details.pan;

    const isCredit =
        card.type === CardType.CREDIT;

    return (
        <div
            className={`
                relative
                min-h-[260px]
                overflow-hidden
                rounded-2xl
                p-6
                shadow-lg
                ${
                isCredit
                    ? "bg-gradient-to-br from-indigo-950 via-indigo-900 to-slate-900"
                    : "bg-gradient-to-br from-slate-900 via-slate-800 to-slate-700"
            }
            `}
        >
            <div
                className="
                    pointer-events-none
                    absolute
                    -right-12
                    -top-12
                    h-40
                    w-40
                    rounded-full
                    bg-white/10
                "
            />

            <div
                className="
                    pointer-events-none
                    absolute
                    -bottom-20
                    -left-10
                    h-48
                    w-48
                    rounded-full
                    bg-white/5
                "
            />

            <div className="relative flex h-full flex-col justify-between gap-8">
                {/* Header */}
                <div className="flex items-start justify-between">
                    <div>
                        <p className="text-xs font-medium uppercase tracking-wider text-white/60">
                            {t(
                                `types.${card.type}`,
                            )}
                        </p>

                        <p className="mt-1 text-sm font-medium text-white">
                            {isCredit
                                ? t("creditCard")
                                : t("debitCard")}
                        </p>
                    </div>

                    {isCredit ? (
                        <CreditCard
                            className="size-7 text-white/80"
                            strokeWidth={1.7}
                        />
                    ) : (
                        <Landmark
                            className="size-7 text-white/80"
                            strokeWidth={1.7}
                        />
                    )}
                </div>

                {/* PAN */}
                <div>
                    <p className="text-xs text-white/60">
                        {t("cardNumber")}
                    </p>

                    <p className="mt-2 font-mono text-xl font-medium tracking-[0.16em] text-white">
                        {formattedPan}
                    </p>
                </div>

                {/* Details */}
                <div className="flex items-end justify-between">
                    <div>
                        <p className="text-[10px] uppercase tracking-wider text-white/50">
                            {t("expires")}
                        </p>

                        <p className="mt-1 font-mono text-sm font-medium text-white">
                            {String(
                                card.expiryMonth,
                            ).padStart(2, "0")}
                            /
                            {String(
                                card.expiryYear,
                            ).slice(-2)}
                        </p>
                    </div>

                    <div>
                        <p className="text-[10px] uppercase tracking-wider text-white/50">
                            {t("cvv")}
                        </p>

                        <p className="mt-1 font-mono text-sm font-medium tracking-widest text-white">
                            {details.cvv}
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}