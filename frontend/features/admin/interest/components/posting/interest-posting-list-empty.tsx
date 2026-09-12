"use client";

import {useTranslations} from "next-intl";

export function InterestPostingListEmpty() {
    const t = useTranslations(
        "admin.interest.postings",
    );

    return (
        <div className="
            rounded-lg
            border
            border-border
            bg-surface
            px-6
            py-12
            text-center
        ">
            <h3 className="
                text-base
                font-semibold
                text-foreground
            ">
                {t("emptyTitle")}
            </h3>

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