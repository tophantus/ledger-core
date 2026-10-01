"use client";

import {CreditCard} from "lucide-react";
import {useTranslations} from "next-intl";

import {getCreditFacilityStatusColor} from "@/lib/utils/credit";

import type {GetUserCreditFacilityResult} from "../types/credit-facility";

interface CreditFacilityHeaderProps {
    facility: GetUserCreditFacilityResult;
}

export function CreditFacilityHeader({
                                         facility,
                                     }: CreditFacilityHeaderProps) {
    const t = useTranslations("credit");

    return (
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
                        {t("facility.cardTitle")}
                    </p>

                    <p className="mt-0.5 text-xs text-text-muted">
                        {t("facility.cardDescription")}
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
    );
}