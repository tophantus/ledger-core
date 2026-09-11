"use client";

import {useLocale, useTranslations} from "next-intl";

import type {WebhookDelivery} from "../types/webhook";

interface WebhookDeliveryListRowProps {
    delivery: WebhookDelivery;
}

export function WebhookDeliveryListRow({
                                           delivery,
                                       }: WebhookDeliveryListRowProps) {
    const t =
        useTranslations("webhook");

    const locale =
        useLocale();

    return (
        <tr className="
            border-b
            border-border
            last:border-b-0
            hover:bg-background-subtle
        ">
            <td className="
                max-w-[260px]
                px-4
                py-4
            ">
                <p className="
                    truncate
                    text-sm
                    font-medium
                    text-foreground
                ">
                    {delivery.eventId}
                </p>
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-4
            ">
                <span className="
                    inline-flex
                    rounded-full
                    bg-secondary
                    px-2
                    py-1
                    text-xs
                    font-medium
                    text-secondary-foreground
                ">
                    {t(
                        `eventTypes.${delivery.eventType}`,
                    )}
                </span>
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-4
            ">
                <span className="
                    inline-flex
                    rounded-full
                    bg-secondary
                    px-2.5
                    py-1
                    text-xs
                    font-medium
                    text-secondary-foreground
                ">
                    {t(
                        `deliveryStatuses.${delivery.status}`,
                    )}
                </span>
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-4
                text-sm
                text-muted
            ">
                {delivery.nextAttemptAt
                    ? new Date(
                        delivery.nextAttemptAt,
                    ).toLocaleString(
                        locale,
                    )
                    : "-"}
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-4
                text-sm
                text-muted
            ">
                {delivery.deliveredAt
                    ? new Date(
                        delivery.deliveredAt,
                    ).toLocaleString(
                        locale,
                    )
                    : "-"}
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-4
                text-sm
                text-muted
            ">
                {new Date(
                    delivery.createdAt,
                ).toLocaleString(
                    locale,
                )}
            </td>
        </tr>
    );
}