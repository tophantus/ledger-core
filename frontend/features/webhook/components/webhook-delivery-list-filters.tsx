"use client";

import {useTranslations} from "next-intl";

import type {
    WebhookDeliveryFilters,
    WebhookDeliveryStatus,
    WebhookEventType,
} from "../types/webhook";

interface WebhookDeliveryListFiltersProps {
    filters: WebhookDeliveryFilters;
    onChange: (
        filters: WebhookDeliveryFilters,
    ) => void;
}

const DELIVERY_STATUSES:
    WebhookDeliveryStatus[] = [
    "PENDING",
    "RETRYING",
    "DELIVERED",
    "FAILED",
];

const EVENT_TYPES:
    WebhookEventType[] = [
    "ACCOUNT_BALANCE_CHANGED",
    "TRANSACTION_COMPLETED",
    "TRANSACTION_FAILED",
];

export function WebhookDeliveryListFilters({
                                               filters,
                                               onChange,
                                           }: WebhookDeliveryListFiltersProps) {
    const t =
        useTranslations("webhook");

    return (
        <div className="
            rounded-lg
            border
            border-border
            bg-surface
            p-4
        ">
            <div className="
                grid
                gap-4
                md:grid-cols-2
            ">
                <div>
                    <label className="
                        mb-1.5
                        block
                        text-sm
                        font-medium
                        text-foreground
                    ">
                        {t(
                            "deliveries.filters.status",
                        )}
                    </label>

                    <select
                        value={
                            filters.status
                            ?? ""
                        }
                        onChange={(event) =>
                            onChange({
                                ...filters,
                                status:
                                    event.target.value
                                        ? event
                                            .target
                                            .value as WebhookDeliveryStatus
                                        : undefined,
                                page: 0,
                            })
                        }
                        className="
                            h-10
                            w-full
                            rounded-md
                            border
                            border-border
                            bg-background
                            px-3
                            text-sm
                            text-foreground
                            outline-none
                            focus:border-primary
                        "
                    >
                        <option value="">
                            {t(
                                "deliveries.filters.allStatuses",
                            )}
                        </option>

                        {DELIVERY_STATUSES.map(
                            (status) => (
                                <option
                                    key={status}
                                    value={status}
                                >
                                    {t(
                                        `deliveryStatuses.${status}`,
                                    )}
                                </option>
                            ),
                        )}
                    </select>
                </div>

                <div>
                    <label className="
                        mb-1.5
                        block
                        text-sm
                        font-medium
                        text-foreground
                    ">
                        {t(
                            "deliveries.filters.eventType",
                        )}
                    </label>

                    <select
                        value={
                            filters.eventType
                            ?? ""
                        }
                        onChange={(event) =>
                            onChange({
                                ...filters,
                                eventType:
                                    event.target.value
                                        ? event
                                            .target
                                            .value as WebhookEventType
                                        : undefined,
                                page: 0,
                            })
                        }
                        className="
                            h-10
                            w-full
                            rounded-md
                            border
                            border-border
                            bg-background
                            px-3
                            text-sm
                            text-foreground
                            outline-none
                            focus:border-primary
                        "
                    >
                        <option value="">
                            {t(
                                "deliveries.filters.allEventTypes",
                            )}
                        </option>

                        {EVENT_TYPES.map(
                            (eventType) => (
                                <option
                                    key={eventType}
                                    value={eventType}
                                >
                                    {t(
                                        `eventTypes.${eventType}`,
                                    )}
                                </option>
                            ),
                        )}
                    </select>
                </div>
            </div>
        </div>
    );
}