"use client";

import {CheckCircle2} from "lucide-react";
import {useTranslations} from "next-intl";
import { CreditRepaymentMandate } from "../types/credit-facility";

interface CreditRepaymentMandateHeaderProps {
    mandate: CreditRepaymentMandate | null;
}

export function CreditRepaymentMandateHeader({
                                                 mandate,
                                             }: CreditRepaymentMandateHeaderProps) {
    const t = useTranslations("credit");

    return (
        <div className="flex items-start justify-between gap-4">
            <div>
                <h2 className="text-sm font-semibold text-text-primary">
                    {t(
                        "facility.repaymentMandate.title",
                    )}
                </h2>

                <p className="mt-1 text-xs text-text-muted">
                    {t(
                        "facility.repaymentMandate.description",
                    )}
                </p>
            </div>

            {mandate && (
                <span
                    className="
                        inline-flex
                        shrink-0
                        items-center
                        gap-1.5
                        rounded-full
                        bg-success
                        px-2.5
                        py-1
                        text-xs
                        font-medium
                        text-background
                    "
                >
                    <CheckCircle2 className="h-3.5 w-3.5" />

                    {t(
                        `facility.repaymentMandate.statuses.${mandate.status}`,
                    )}
                </span>
            )}
        </div>
    );
}