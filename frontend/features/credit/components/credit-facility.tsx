"use client";

import {useTranslations} from "next-intl";

import type {GetUserCreditFacilityResult} from "../types/credit-facility";
import {CreditFacilityHeader} from "./credit-facility-header";
import {CreditFacilityMeta} from "./credit-facility-meta";
import {CreditFacilitySummary} from "./credit-facility-summary";
import {CreditRepaymentMandate} from "./credit-repayment-mandate";

interface CreditFacilityProps {
    facility: GetUserCreditFacilityResult | null;
}

export function CreditFacility({
                                   facility,
                               }: CreditFacilityProps) {
    const t = useTranslations("credit");

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
                    overflow-hidden
                    rounded-2xl
                    border
                    border-border
                    bg-surface
                    shadow-sm
                "
            >
                <CreditFacilityHeader facility={facility} />

                <div className="p-5 sm:p-6">
                    <CreditFacilitySummary
                        facility={facility}
                    />

                    <CreditRepaymentMandate
                        facility={facility}
                    />

                    <CreditFacilityMeta
                        openedAt={facility.openedAt}
                    />
                </div>
            </div>
        </section>
    );
}