"use client";

import {
    Trash2,
} from "lucide-react";
import {
    useLocale,
    useTranslations,
} from "next-intl";
import {useState} from "react";

import {Button} from "@/components/ui/button";
import {useDeleteWebhook} from "../hooks/use-delete-webhook";
import type {
    Webhook,
} from "../types/webhook";

import {
    getWebhookStatusColor,
} from "@/lib/utils/webhook";

interface WebhookListRowProps {
    webhook: Webhook;
    accountNo?: string;
    onRemoved: () => void;
}

export function WebhookListRow({
                                   webhook,
                                   accountNo,
                                   onRemoved,
                               }: WebhookListRowProps) {
    const t =
        useTranslations("webhook");

    const locale =
        useLocale();

    const {
        removeWebhook,
    } = useDeleteWebhook();

    const [
        isRemoving,
        setIsRemoving,
    ] = useState(false);

    const handleRemove = async () => {
        if (isRemoving) {
            return;
        }

        setIsRemoving(true);

        try {
            const response =
                await removeWebhook(
                    webhook.id,
                );

            if (response.success) {
                onRemoved();
            }
        } finally {
            setIsRemoving(false);
        }
    };

    return (
        <tr className="
            border-b
            border-border
            last:border-b-0
            hover:bg-background-subtle
        ">
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
                                    bg-background-subtle
                                    px-2
                                    py-1
                                    text-xs
                                    font-medium
                                    text-muted
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
            ">
                {webhook.status === "ACTIVE" && (
                    <Button
                        type="button"
                        variant="danger"
                        loading={isRemoving}
                        onClick={
                            handleRemove
                        }
                    >
                        <Trash2 className="h-3.5 w-3.5" />

                        {t(
                            "list.remove",
                        )}
                    </Button>
                )}
            </td>
        </tr>
    );
}