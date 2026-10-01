"use client";

import {
    LockKeyhole,
    Wallet,
} from "lucide-react";
import {
    useLocale,
    useTranslations,
} from "next-intl";

import {formatMoney} from "@/lib/utils/currency";

import type {GetUserCreditFacilityResult} from "../types/credit-facility";

interface CreditFacilitySummaryProps {
    facility: GetUserCreditFacilityResult;
}

export function CreditFacilitySummary({
                                          facility,
                                      }: CreditFacilitySummaryProps) {
    const t = useTranslations("credit");
    const locale = useLocale();

    return (
        <>
            {/* Available Credit */}
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
                        {t("facility.availableCredit")}
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
                <SummaryItem
                    label={t("facility.creditLimit")}
                    value={formatMoney(
                        facility.creditLimit,
                        facility.currency,
                        locale,
                    )}
                />

                <SummaryItem
                    label={t(
                        "facility.outstandingBalance",
                    )}
                    value={formatMoney(
                        facility.outstandingBalance,
                        facility.currency,
                        locale,
                    )}
                />

                <SummaryItem
                    label={t("facility.holdAmount")}
                    value={formatMoney(
                        facility.holdAmount,
                        facility.currency,
                        locale,
                    )}
                    icon={
                        <LockKeyhole className="h-3.5 w-3.5 text-text-muted" />
                    }
                />
            </div>
        </>
    );
}

interface SummaryItemProps {
    label: string;
    value: string;
    icon?: React.ReactNode;
}

function SummaryItem({
                         label,
                         value,
                         icon,
                     }: SummaryItemProps) {
    return (
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
                {icon}

                <p className="text-xs font-medium text-text-muted">
                    {label}
                </p>
            </div>

            <p className="mt-2 text-base font-semibold text-text-primary">
                {value}
            </p>
        </div>
    );
}