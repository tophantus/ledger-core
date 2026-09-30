"use client";

import {CalendarDays} from "lucide-react";
import {
    useLocale,
    useTranslations,
} from "next-intl";

interface CreditFacilityMetaProps {
    openedAt: string;
}

export function CreditFacilityMeta({
                                       openedAt,
                                   }: CreditFacilityMetaProps) {
    const t = useTranslations("credit");
    const locale = useLocale();

    const formattedOpenedAt =
        new Intl.DateTimeFormat(locale, {
            year: "numeric",
            month: "short",
            day: "numeric",
        }).format(new Date(openedAt));

    return (
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
                {t("facility.openedAt")}:{" "}
                {formattedOpenedAt}
            </span>
        </div>
    );
}