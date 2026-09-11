"use client";

import {useLocale, useTranslations} from "next-intl";

import type {Webhook} from "../types/webhook";

import {getWebhookStatusColor} from "@/lib/utils/webhook";

interface WebhookDetailsSummaryProps {
    webhook: Webhook;
    accountNo?: string;
}

export function WebhookDetailsSummary({
                                          webhook,
                                          accountNo,
                                      }: WebhookDetailsSummaryProps) {
    const t =
        useTranslations("webhook");

    const locale =
        useLocale();

    return (
        <div className="
            rounded-lg
            border
            border-border
            bg-surface
            p-6
        ">
            <div className="
                grid
                gap-6
                md:grid-cols-2
            ">
                <div>
                    <p className="
                        text-xs
                        font-medium
                        text-muted
                    ">
                        {t("details.url")}
                    </p>

                    <p className="
                        mt-1
                        break-all
                        text-sm
                        font-medium
                        text-foreground
                    ">
                        {webhook.url}
                    </p>
                </div>

                <div>
                    <p className="
                        text-xs
                        font-medium
                        text-muted
                    ">
                        {t("details.account")}
                    </p>

                    <p className="
                        mt-1
                        text-sm
                        font-medium
                        text-foreground
                    ">
                        {accountNo ?? webhook.accountId}
                    </p>
                </div>

                <div>
                    <p className="
                        text-xs
                        font-medium
                        text-muted
                    ">
                        {t("details.status")}
                    </p>

                    <span
                        className={`
                            mt-1
                            inline-flex
                            rounded-full
                            px-2.5
                            py-1
                            text-xs
                            font-medium
                            ${getWebhookStatusColor(
                            webhook.status,
                            "text-background",
                        )}
                        `}
                    >
                        {t(
                            `statuses.${webhook.status}`,
                        )}
                    </span>
                </div>

                <div>
                    <p className="
                        text-xs
                        font-medium
                        text-muted
                    ">
                        {t("details.events")}
                    </p>

                    <div className="
                        mt-1
                        flex
                        flex-wrap
                        gap-1.5
                    ">
                        {webhook.eventTypes.map(
                            (eventType) => (
                                <span
                                    key={eventType}
                                    className="
                                        inline-flex
                                        rounded-full
                                        bg-secondary
                                        px-2
                                        py-1
                                        text-xs
                                        font-medium
                                        text-secondary-foreground
                                    "
                                >
                                    {t(
                                        `eventTypes.${eventType}`,
                                    )}
                                </span>
                            ),
                        )}
                    </div>
                </div>

                <div>
                    <p className="
                        text-xs
                        font-medium
                        text-muted
                    ">
                        {t("details.createdAt")}
                    </p>

                    <p className="
                        mt-1
                        text-sm
                        text-foreground
                    ">
                        {new Date(
                            webhook.createdAt,
                        ).toLocaleString(
                            locale,
                        )}
                    </p>
                </div>

                <div>
                    <p className="
                        text-xs
                        font-medium
                        text-muted
                    ">
                        {t("details.updatedAt")}
                    </p>

                    <p className="
                        mt-1
                        text-sm
                        text-foreground
                    ">
                        {new Date(
                            webhook.updatedAt,
                        ).toLocaleString(
                            locale,
                        )}
                    </p>
                </div>
            </div>
        </div>
    );
}