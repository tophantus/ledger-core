"use client";

import {
    X,
} from "lucide-react";
import {
    useLocale,
    useTranslations,
} from "next-intl";
import {useState} from "react";

import type {
    WithdrawalIntent,
} from "../../types/withdrawal";

import {useCancelWithdrawalIntent} from "../../hooks/use-cancel-withdrawal-intent";

import {formatMoney} from "@/lib/utils/currency";
import {
    getWithdrawalStatusColor,
} from "@/lib/utils/withdrawal";

interface WithdrawalListRowProps {
    withdrawal: WithdrawalIntent;
    onCancelled: () => void;
}

export function WithdrawalListRow({
                                      withdrawal,
                                      onCancelled,
                                  }: WithdrawalListRowProps) {
    const t =
        useTranslations("withdrawal");

    const locale = useLocale();

    const {
        cancelWithdrawalIntent,
    } =
        useCancelWithdrawalIntent();

    const [isCancelling, setIsCancelling] =
        useState(false);

    const handleCancel = async () => {
        if (isCancelling) {
            return;
        }

        setIsCancelling(true);

        try {
            const response =
                await cancelWithdrawalIntent(
                    withdrawal.id,
                );

            if (response.success) {
                onCancelled();
            }
        } finally {
            setIsCancelling(false);
        }
    };

    return (
        <tr className="
            border-b
            border-border
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

            <td className="
                whitespace-nowrap
                px-4
                py-4
                text-right
            ">
                {withdrawal.status === "READY" && (
                    <button
                        type="button"
                        onClick={
                            handleCancel
                        }
                        disabled={
                            isCancelling
                        }
                        className="
                            inline-flex
                            items-center
                            gap-2
                            rounded-md
                            border
                            border-danger
                            px-3
                            py-1.5
                            text-xs
                            font-medium
                            text-danger
                            transition
                            hover:bg-danger/10
                            disabled:cursor-not-allowed
                            disabled:opacity-50
                        "
                    >
                        <X className="h-3.5 w-3.5" />

                        {isCancelling
                            ? t(
                                "list.cancelling",
                            )
                            : t(
                                "list.cancel",
                            )}
                    </button>
                )}
            </td>
        </tr>
    );
}