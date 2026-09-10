"use client";

import {
    useLocale,
    useTranslations,
} from "next-intl";

import type {
    WithdrawalIntent,
} from "../../types/withdrawal";

import {formatMoney} from "@/lib/utils/currency";
import {
    getWithdrawalStatusColor,
} from "@/lib/utils/withdrawal";

interface WithdrawalListRowProps {
    withdrawal: WithdrawalIntent;
}

export function WithdrawalListRow({
                                      withdrawal,
                                  }: WithdrawalListRowProps) {
    const t =
        useTranslations("withdrawal");

    const locale = useLocale();

    return (
        <tr className="
            border-b
            border-border
            last:border-b-0
            hover:bg-background-subtle
        ">
            <td className="
                whitespace-nowrap
                px-4
                py-4
            ">
                <span className="
                    font-mono
                    text-xs
                    font-medium
                    text-primary
                ">
                    {
                        withdrawal.withdrawalReference
                    }
                </span>
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-4
                text-sm
                text-foreground
            ">
                {withdrawal.accountId}
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-4
                text-right
                font-medium
                text-foreground
            ">
                {formatMoney(
                    withdrawal.amount,
                    withdrawal.currency,
                    locale,
                )}
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
                        ${getWithdrawalStatusColor(
                        withdrawal.status,
                        "text-background",
                    )}
                    `}
                >
                    {t(
                        `statuses.${withdrawal.status}`,
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
                    withdrawal.createdAt,
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
                {withdrawal.completedAt
                    ? new Date(
                        withdrawal.completedAt,
                    ).toLocaleString(
                        locale,
                    )
                    : "—"}
            </td>
        </tr>
    );
}