"use client";

import {useTranslations} from "next-intl";

import type {WebhookDelivery} from "../types/webhook";

import {WebhookDeliveryListRow} from "./webhook-delivery-list-row";

interface WebhookDeliveryListTableProps {
    deliveries: WebhookDelivery[];
}

export function WebhookDeliveryListTable({
                                             deliveries,
                                         }: WebhookDeliveryListTableProps) {
    const t =
        useTranslations("webhook");

    return (
        <div className="
            overflow-hidden
            rounded-lg
            border
            border-border
            bg-surface
        ">
            <div className="overflow-x-auto">
                <table className="
                    w-full
                    min-w-[1100px]
                    text-sm
                ">
                    <thead>
                    <tr className="
                        border-b
                        border-border
                        bg-background-subtle
                    ">
                        <th className="
                            whitespace-nowrap
                            px-4
                            py-3
                            text-left
                            text-xs
                            font-medium
                            text-muted
                        ">
                            {t(
                                "deliveries.eventId",
                            )}
                        </th>

                        <th className="
                            whitespace-nowrap
                            px-4
                            py-3
                            text-left
                            text-xs
                            font-medium
                            text-muted
                        ">
                            {t(
                                "deliveries.eventType",
                            )}
                        </th>

                        <th className="
                            whitespace-nowrap
                            px-4
                            py-3
                            text-left
                            text-xs
                            font-medium
                            text-muted
                        ">
                            {t(
                                "deliveries.status",
                            )}
                        </th>

                        <th className="
                            whitespace-nowrap
                            px-4
                            py-3
                            text-left
                            text-xs
                            font-medium
                            text-muted
                        ">
                            {t(
                                "deliveries.nextAttemptAt",
                            )}
                        </th>

                        <th className="
                            whitespace-nowrap
                            px-4
                            py-3
                            text-left
                            text-xs
                            font-medium
                            text-muted
                        ">
                            {t(
                                "deliveries.deliveredAt",
                            )}
                        </th>

                        <th className="
                            whitespace-nowrap
                            px-4
                            py-3
                            text-left
                            text-xs
                            font-medium
                            text-muted
                        ">
                            {t(
                                "deliveries.createdAt",
                            )}
                        </th>
                    </tr>
                    </thead>

                    <tbody>
                    {deliveries.map(
                        (delivery) => (
                            <WebhookDeliveryListRow
                                key={
                                    delivery.id
                                }
                                delivery={
                                    delivery
                                }
                            />
                        ),
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}