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
    const isIncoming =
        type === "DEPOSIT" ||
        type === "REFUND";

    return `${isIncoming ? "+" : "-"}${amount}`;
}

function formatDate(value: string): string {
    return new Date(value).toLocaleString();
}

export function TransactionRow({
                                   transaction,
                               }: TransactionRowProps) {
    const t = useTranslations("transaction");

    const isIncoming =
        transaction.type === "DEPOSIT" ||
        transaction.type === "REFUND";

    return (
        <div className="flex gap-4 border-b border-border py-5 last:border-b-0">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-secondary">
                {isIncoming ? (
                    <ArrowDownLeft className="h-5 w-5 text-text-primary" />
                ) : (
                    <ArrowUpRight className="h-5 w-5 text-text-primary" />
                )}
            </div>

            <div className="min-w-0 flex-1">
                <div className="flex flex-col justify-between gap-2 sm:flex-row">
                    <div className="min-w-0">
                        <p className="truncate text-sm font-medium text-text-primary">
                            {t(
                                `types.${transaction.type}`,
                            )}
                        </p>

                        <p className="truncate text-xs text-text-muted">
                            {transaction.reference}
                        </p>

                        {transaction.description && (
                            <p className="mt-1 truncate text-xs text-text-muted">
                                {
                                    transaction.description
                                }
                            </p>
                        )}
                    </div>

                    <div className="shrink-0 text-left sm:text-right">
                        <p className="text-sm font-semibold text-text-primary">
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

                <p className="mt-2 text-xs text-text-muted">
                    {formatDate(
                        transaction.createdAt,
                    )}
                </p>
            </div>
        </div>
    );
}