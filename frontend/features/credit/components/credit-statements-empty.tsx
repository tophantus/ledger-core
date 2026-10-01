"use client";

import {FileText} from "lucide-react";
import {useTranslations} from "next-intl";

export function CreditStatementsEmpty() {
    const t = useTranslations("credit");

    return (
        <div
            className="
                flex flex-col items-center
                rounded-xl border border-dashed
                border-border bg-surface-subtle
                px-6 py-10 text-center
            "
        >
            <div
                className="
                    flex h-12 w-12 items-center
                    justify-center rounded-full
                    bg-background
                "
            >
                <FileText className="h-6 w-6 text-text-muted" />
            </div>

            <h3 className="mt-4 text-sm font-semibold text-text-primary">
                {t("statements.empty.title")}
            </h3>

            <p className="mt-1 max-w-md text-sm text-text-muted">
                {t("statements.empty.description")}
            </p>
        </div>
    );
}