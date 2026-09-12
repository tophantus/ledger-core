"use client";

import {useTranslations} from "next-intl";

export function InterestAccrualListEmpty() {
    const t = useTranslations(
        "admin.interest.accruals",
    );

    return (
        <div className="
            flex
            flex-col
            items-center
            justify-center
            rounded-md
            border
            border-dashed
            border-border
            px-6
            py-12
            text-center
        ">
            <p className="
                text-sm
                font-medium
                text-foreground
            ">
                {t("emptyTitle")}
            </p>

            <p className="
                mt-1
                text-sm
                text-muted
            ">
                {t("emptyDescription")}
            </p>
        </div>
    );
}