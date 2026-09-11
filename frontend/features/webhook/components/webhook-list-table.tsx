"use client";

import {useTranslations} from "next-intl";

import type {
    AccountSummary,
} from "@/features/account/types/account";

import type {
    Webhook,
} from "../types/webhook";

import {WebhookListRow} from "./webhook-list-row";

interface WebhookListTableProps {
    webhooks: Webhook[];
    accounts: AccountSummary[];
    onRemoved: () => void;
}

export function WebhookListTable({
                                     webhooks,
                                     accounts,
                                     onRemoved,
                                 }: WebhookListTableProps) {
    const t =
        useTranslations("webhook");

    const getAccountNo = (
        accountId: string,
    ) => {
        return accounts.find(
            (account) =>
                account.id === accountId,
        )?.accountNo;
    };

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
                                "list.url",
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
                                "list.account",
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
                                "list.events",
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
                                "list.status",
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
                                "list.createdAt",
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
                                "list.updatedAt",
                            )}
                        </th>

                        <th className="
                            whitespace-nowrap
                            px-4
                            py-3
                            text-right
                            text-xs
                            font-medium
                            text-muted
                        ">
                            {t(
                                "list.actions",
                            )}
                        </th>
                    </tr>
                    </thead>

                    <tbody>
                    {webhooks.map(
                        (webhook) => (
                            <WebhookListRow
                                key={
                                    webhook.id
                                }
                                webhook={
                                    webhook
                                }
                                accountNo={
                                    getAccountNo(
                                        webhook.accountId,
                                    )
                                }
                                onRemoved={
                                    onRemoved
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