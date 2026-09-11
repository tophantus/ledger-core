"use client";

import {useTranslations} from "next-intl";

import type {Transaction} from "../../types/transaction";

import {TransactionRow} from "./transaction-row";
import {TransactionSkeleton} from "../transaction-skeleton";

interface TransactionListProps {
    transactions: Transaction[];
    isLoading: boolean;
    hasError: boolean;
}

export function TransactionList({
                                    transactions,
                                    isLoading,
                                    hasError,
                                }: TransactionListProps) {
    const t = useTranslations("transaction");

    if (isLoading) {
        return (
            <div className="rounded-lg border border-border bg-surface px-6">
                {Array.from({length: 5}).map(
                    (_, index) => (
                        <TransactionSkeleton
                            key={index}
                        />
                    ),
                )}
            </div>
        );
    }

    if (hasError) {
        return (
            <div className="rounded-lg border border-border bg-surface p-8 text-center">
                <p className="text-sm text-muted">
                    {t("list.loadError")}
                </p>
            </div>
        );
    }

    if (transactions.length === 0) {
        return (
            <div className="rounded-lg border border-dashed border-border bg-surface p-8 text-center">
                <p className="text-sm text-muted">
                    {t("list.empty")}
                </p>
            </div>
        );
    }

    return (
        <div className="rounded-lg border border-border bg-surface px-6">
            {transactions.map((transaction) => (
                <TransactionRow
                    key={transaction.id}
                    transaction={transaction}
                />
            ))}
        </div>
    );
}