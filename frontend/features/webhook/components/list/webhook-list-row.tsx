"use client";

import {
    useLocale,
    useTranslations,
} from "next-intl";
import type {
    Webhook,
} from "../../types/webhook";

import {
    getWebhookStatusColor,
} from "@/lib/utils/webhook";
import {WebhookActions} from "@/features/webhook/components/webhook-actions";
import {useRouter} from "next/navigation";
import {ROUTES} from "@/lib/constants/routes";

interface WebhookListRowProps {
    webhook: Webhook;
    accountNo?: string;
    onRemoved: () => void;
    onUpdated: () => void;
}

export function WebhookListRow({
                                   webhook,
                                   accountNo,
                                   onRemoved,
                                   onUpdated,
                               }: WebhookListRowProps) {
    const t =
        useTranslations("webhook");

    const locale =
        useLocale();

    const router =
        useRouter();

    const handleClick = () => {
        router.push(
            ROUTES.WEBHOOKS.DETAILS(webhook.id),
        );
    };

    return (
        <tr className="
                border-b
                border-border
                last:border-b-0
                hover:bg-background-subtle"
            onClick={handleClick}
        >
            <td className="
                max-w-[280px]
                px-4
                py-4
            ">
                <p className="
                    truncate
                    text-sm
                    font-medium
                    text-foreground
                ">
                    {webhook.url}
                </p>
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-4
                text-sm
                text-foreground
            ">
                {accountNo ?? webhook.accountId}
            </td>

            <td className="
                px-4
                py-4
            ">
                <div className="
                    flex
                    max-w-[260px]
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
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-4
            ">
                <span
                    className={`
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
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-4
                text-sm
                text-muted
            ">
                {new Date(
                    webhook.createdAt,
                ).toLocaleString(
                    locale,
                )}
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-4
                text-sm
                text-muted
            ">
                {new Date(
                    webhook.updatedAt,
                ).toLocaleString(
                    locale,
                )}
            </td>

            <td className="
                    whitespace-nowrap
                    px-4
                    py-4
                    text-right
                "
                onClick={(event) => {
                    event.stopPropagation();
                }}
            >
                <WebhookActions
                    webhook={webhook}
                    onRemoved={onRemoved}
                    onUpdated={onUpdated}
                />
            </td>
        </tr>
    );
}