"use client";

import {useLocale, useTranslations} from "next-intl";

import {getCreditFacilityStatusColor} from "@/lib/utils/credit";
import {formatMoney} from "@/lib/utils/currency";

import type {GetUserCreditFacilityResult} from "../types/credit-facility";

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
                    rounded-xl
                    border
                    border-border
                    bg-surface
                    p-5
                    shadow-sm
                "
            >
                <div
                    className="
                        grid
                        gap-5
                        sm:grid-cols-2
                        lg:grid-cols-4
                    "
                >
                    {/* Credit Limit */}
                    <div>
                        <p className="text-xs font-medium text-text-muted">
                            {t(
                                "facility.creditLimit",
                            )}
                        </p>

                        <p className="mt-2 text-lg font-semibold text-text-primary">
                            {formatMoney(
                                facility.creditLimit,
                                facility.currency,
                                locale,
                            )}
                        </p>
                    </div>

                    {/* Outstanding Balance */}
                    <div>
                        <p className="text-xs font-medium text-text-muted">
                            {t(
                                "facility.outstandingBalance",
                            )}
                        </p>

                        <p className="mt-2 text-lg font-semibold text-text-primary">
                            {formatMoney(
                                facility.outstandingBalance,
                                facility.currency,
                                locale,
                            )}
                        </p>
                    </div>

                    {/* Status */}
                    <div>
                        <p className="text-xs font-medium text-text-muted">
                            {t("facility.status")}
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
                    </div>

                    {/* Opened At */}
                    <div>
                        <p className="text-xs font-medium text-text-muted">
                            {t(
                                "facility.openedAt",
                            )}
                        </p>

                        <p className="mt-2 text-sm font-medium text-text-primary">
                            {new Date(
                                facility.openedAt,
                            ).toLocaleDateString(
                                locale,
                            )}
                        </p>
                    </div>
                </div>
            </div>
        </section>
    );
}