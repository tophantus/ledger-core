"use client";

import {
    ArrowDownLeft,
    ArrowUpRight,
} from "lucide-react";
import {useTranslations} from "next-intl";

import type {Transaction} from "../types/transaction";

interface TransactionRowProps {
    transaction: Transaction;
}

function formatAmount(
    amount: string,
    type: Transaction["type"],
): string {
    const prefix =
        type === "DEPOSIT" ||
        type === "REFUND"
            ? "+"
            : "-";

    return `${prefix}${amount}`;
}

export function TransactionRow({
                                   transaction,
                               }: TransactionRowProps) {
    const t = useTranslations("transaction");

    const isIncoming =
        transaction.type === "DEPOSIT" ||
        transaction.type === "REFUND";

    return (
        <div className="flex items-center justify-between gap-4 border-b border-border py-4 last:border-b-0">
            <div className="flex min-w-0 items-center gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-background-subtle">
                    {isIncoming ? (
                        <ArrowDownLeft className="h-5 w-5 text-text-primary" />
                    ) : (
                        <ArrowUpRight className="h-5 w-5 text-text-primary" />
                    )}
                </div>

                <div className="min-w-0">
                    <p className="truncate text-sm font-medium text-text-primary">
                        {t(
                            `types.${transaction.type}`,
                        )}
                    </p>

                    <p className="text-xs text-text-muted">
                        {transaction.reference}
                    </p>
                </div>
            </div>

            <div className="shrink-0 text-right">
                <p className="text-sm font-medium text-text-primary">
                    {formatAmount(
                        transaction.amount,
                        transaction.type,
                    )}{" "}
                    {transaction.currency}
                </p>

                <p className="text-xs text-text-muted">
                    {t(
                        `statuses.${transaction.status}`,
                    )}
                </p>
            </div>
        </div>
    );
}