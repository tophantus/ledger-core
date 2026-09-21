"use client";

import {
    CalendarDays,
    CreditCard,
    LockKeyhole,
    Wallet,
} from "lucide-react";
import {
    useLocale,
    useTranslations,
} from "next-intl";

import {getCreditFacilityStatusColor} from "@/lib/utils/credit";
import {formatMoney} from "@/lib/utils/currency";

import type {
    GetUserCreditFacilityResult,
} from "../types/credit-facility";

interface CreditFacilityProps {
    facility:
        | GetUserCreditFacilityResult
        | null;
}

export function CreditFacility({
                                   facility,
                               }: CreditFacilityProps) {
    const t = useTranslations("credit");
    const locale = useLocale();

    if (!facility) {
        return (
            <section className="space-y-6">
                <div>
                    <h1 className="text-2xl font-semibold text-primary">
                        {t("facility.title")}
                    </h1>

                    <p className="mt-1 text-sm text-text-muted">
                        {t("facility.empty")}
                    </p>
                </div>
            </section>
        );
    }

    const openedAt =
        new Intl.DateTimeFormat(locale, {
            year: "numeric",
            month: "short",
            day: "numeric",
        }).format(
            new Date(facility.openedAt),
        );

    return (
        <section className="space-y-6">
            <div>
                <h1 className="text-2xl font-semibold text-primary">
                    {t("facility.title")}
                </h1>

                <p className="mt-1 text-sm text-text-muted">
                    {t("facility.description")}
                </p>
            </div>

            <div
                className="
                    overflow-hidden
                    rounded-2xl
                    border
                    border-border
                    bg-surface
                    shadow-sm
                "
            >
                {/* Header */}
                <div
                    className="
                        flex
                        items-start
                        justify-between
                        gap-4
                        border-b
                        border-border
                        p-5
                        sm:p-6
                    "
                >
                    <div className="flex items-center gap-3">
                        <div
                            className="
                                flex
                                h-11
                                w-11
                                shrink-0
                                items-center
                                justify-center
                                rounded-xl
                                bg-primary-subtle
                                text-primary
                            "
                        >
                            <CreditCard className="h-5 w-5" />
                        </div>

                        <div>
                            <p className="text-sm font-semibold text-text-primary">
                                {t(
                                    "facility.cardTitle",
                                )}
                            </p>

                            <p className="mt-0.5 text-xs text-text-muted">
                                {t(
                                    "facility.cardDescription",
                                )}
                            </p>
                        </div>
                    </div>

                    <span
                        className={`
                            inline-flex
                            shrink-0
                            items-center
                            rounded-full
                            px-2.5
                            py-1
                            text-xs
                            font-medium
                            ${getCreditFacilityStatusColor(
                            facility.status,
                            "text-background",
                        )}
                        `}
                    >
                        {t(
                            `facility.statuses.${facility.status}`,
                        )}
                    </span>
                </div>

                {/* Available Credit */}
                <div className="p-5 sm:p-6">
                    <div
                        className="
                            rounded-xl
                            border
                            border-primary/10
                            bg-primary-subtle
                            p-4
                            sm:p-5
                        "
                    >
                        <div className="flex items-center gap-2">
                            <Wallet className="h-4 w-4 text-primary" />

                            <p className="text-sm font-medium text-text-secondary">
                                {t(
                                    "facility.availableCredit",
                                )}
                            </p>
                        </div>

                        <p className="mt-2 text-2xl font-bold tracking-tight text-primary sm:text-3xl">
                            {formatMoney(
                                facility.availableCredit,
                                facility.currency,
                                locale,
                            )}
                        </p>
                    </div>

                    {/* Financial Summary */}
                    <div
                        className="
                            mt-5
                            grid
                            gap-4
                            sm:grid-cols-3
                        "
                    >
                        <div
                            className="
                                rounded-xl
                                border
                                border-border
                                bg-background-subtle
                                p-4
                            "
                        >
                            <p className="text-xs font-medium text-text-muted">
                                {t(
                                    "facility.creditLimit",
                                )}
                            </p>

                            <p className="mt-2 text-base font-semibold text-text-primary">
                                {formatMoney(
                                    facility.creditLimit,
                                    facility.currency,
                                    locale,
                                )}
                            </p>
                        </div>

                        <div
                            className="
                                rounded-xl
                                border
                                border-border
                                bg-background-subtle
                                p-4
                            "
                        >
                            <div className="flex items-center gap-1.5">
                                <p className="text-xs font-medium text-text-muted">
                                    {t(
                                        "facility.outstandingBalance",
                                    )}
                                </p>
                            </div>

                            <p className="mt-2 text-base font-semibold text-text-primary">
                                {formatMoney(
                                    facility.outstandingBalance,
                                    facility.currency,
                                    locale,
                                )}
                            </p>
                        </div>

                        <div
                            className="
                                rounded-xl
                                border
                                border-border
                                bg-background-subtle
                                p-4
                            "
                        >
                            <div className="flex items-center gap-1.5">
                                <LockKeyhole className="h-3.5 w-3.5 text-text-muted" />

                                <p className="text-xs font-medium text-text-muted">
                                    {t(
                                        "facility.holdAmount",
                                    )}
                                </p>
                            </div>

                            <p className="mt-2 text-base font-semibold text-text-primary">
                                {formatMoney(
                                    facility.holdAmount,
                                    facility.currency,
                                    locale,
                                )}
                            </p>
                        </div>
                    </div>

                    {/* Meta */}
                    <div
                        className="
                            mt-5
                            flex
                            items-center
                            gap-2
                            border-t
                            border-border
                            pt-4
                            text-xs
                            text-text-muted
                        "
                    >
                        <CalendarDays className="h-3.5 w-3.5" />

                        <span>
                            {t(
                                "facility.openedAt",
                            )}
                            : {openedAt}
                        </span>
                    </div>
                </div>
            </div>
        </section>
    );
}