"use client";

import {useTranslations} from "next-intl";

export function WebhookDeliveryListEmpty() {
    const t =
        useTranslations("webhook");

    return (
        <div className="
            rounded-lg
            border
            border-border
            bg-surface
            p-8
            text-center
        ">
            <h3 className="
                text-sm
                font-semibold
                text-foreground
            ">
                {t(
                    "deliveries.emptyTitle",
                )}
            </h3>

            <p className="
                mt-1
                text-sm
                text-muted
            ">
                {t(
                    "deliveries.emptyDescription",
                )}
            </p>
        </div>
    );
}