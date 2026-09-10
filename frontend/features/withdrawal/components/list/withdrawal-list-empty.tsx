"use client";

import {ArrowDownToLine} from "lucide-react";
import {useTranslations} from "next-intl";

export function WithdrawalListEmpty() {
    const t =
        useTranslations("withdrawal");

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
            <div className="
                mx-auto
                flex
                h-12
                w-12
                items-center
                justify-center
                rounded-full
                bg-background-subtle
                text-muted
            ">
                <ArrowDownToLine className="h-5 w-5" />
            </div>

            <h2 className="
                mt-4
                text-sm
                font-semibold
                text-foreground
            ">
                {t(
                    "list.emptyTitle",
                )}
            </h2>

            <p className="
                mt-1
                text-sm
                text-muted
            ">
                {t(
                    "list.emptyDescription",
                )}
            </p>
        </div>
    );
}