"use client";

import {useTranslations} from "next-intl";
import {CreditCard, Landmark} from "lucide-react";

import {
    getCardStatusColor,
} from "@/lib/utils/card";

import {
    CardForm,
    CardType,
    type CardInfo,
} from "../types/card";

interface CardItemProps {
    card: CardInfo;
}

export function CardItem({
                             card,
                         }: CardItemProps) {
    const t = useTranslations("card");

    const isCredit =
        card.type === CardType.CREDIT;

    const isVirtual =
        card.form === CardForm.VIRTUAL;

    return (
        <div
            className={`
                group
                relative
                min-h-[220px]
                overflow-hidden
                rounded-2xl
                p-6
                shadow-md
                transition-all
                duration-200
                hover:-translate-y-1
                hover:shadow-lg
                ${
                isCredit
                    ? "bg-gradient-to-br from-indigo-950 via-indigo-900 to-slate-900"
                    : "bg-gradient-to-br from-slate-900 via-slate-800 to-slate-700"
            }
            `}
        >
            {/* Decorative background */}
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
                                ? t(
                                    "creditCard",
                                )
                                : t(
                                    "debitCard",
                                )}
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

                {/* Card Number */}
                <div>
                    <p className="text-xs text-white/60">
                        {t("cardNumber")}
                    </p>

                    <p className="mt-2 font-mono text-lg font-medium tracking-[0.18em] text-white">
                        •••• •••• ••••{" "}
                        {card.panLast4}
                    </p>
                </div>

                {/* Footer */}
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

                    <div className="text-right">
                        <span
                            className={`
                                inline-flex
                                items-center
                                rounded-full
                                px-2.5
                                py-1
                                text-xs
                                font-medium
                                ${getCardStatusColor(
                                card.status,
                                "text-background",
                            )}
                            `}
                        >
                            {t(
                                `statuses.${card.status}`,
                            )}
                        </span>

                        <p className="mt-1 text-[10px] text-white/50">
                            {isVirtual
                                ? t(
                                    "virtual",
                                )
                                : t(
                                    "physical",
                                )}
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}