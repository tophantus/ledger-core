"use client";

import {
    CheckCircle2,
} from "lucide-react";
import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";

import type {
    Transaction,
} from "@/features/transaction/types/transaction";

import {formatMoney} from "@/lib/utils/currency";

interface TransferResultProps {
    transaction: Transaction;
    locale: string;
    onViewTransactions: () => void;
    onNewTransfer: () => void;
}

export function TransferResult({
                                   transaction,
                                   locale,
                                   onViewTransactions,
                                   onNewTransfer,
                               }: TransferResultProps) {
    const t =
        useTranslations("transaction");

    return (
        <div
            className="
                rounded-lg
                border
                border-border
                bg-surface
                p-6
            "
        >
            <div className="flex flex-col items-center text-center">
                <div className="flex h-14 w-14 items-center justify-center rounded-full bg-success">
                    <CheckCircle2 className="h-7 w-7 text-success-foreground" />
                </div>

                <h2 className="mt-4 text-xl font-semibold text-success">
                    {t(
                        "transfer.successTitle",
                    )}
                </h2>

                <p className="mt-1 text-sm text-muted">
                    {t(
                        "transfer.successDescription",
                    )}
                </p>
            </div>

            <div className="mt-6 divide-y divide-border rounded-lg border">
                <ResultRow
                    label={t(
                        "transfer.transactionId",
                    )}
                    value={
                        transaction.id
                    }
                />

                <ResultRow
                    label={t(
                        "transfer.reference",
                    )}
                    value={
                        transaction.reference
                    }
                />

                <ResultRow
                    label={t(
                        "transfer.amount",
                    )}
                    value={formatMoney(
                        transaction.amount,
                        transaction.currency,
                        locale,
                    )}
                />

                <ResultRow
                    label={t(
                        "transfer.status",
                    )}
                    value={t(
                        `statuses.${transaction.status}`,
                    )}
                />
            </div>

            <div className="mt-6 flex flex-col gap-3 sm:flex-row">
                <Button
                    type="button"
                    variant="outline"
                    onClick={
                        onViewTransactions
                    }
                >
                    {t(
                        "transfer.viewTransactions",
                    )}
                </Button>

                <Button
                    type="button"
                    onClick={onNewTransfer}
                >
                    {t(
                        "transfer.newTransfer",
                    )}
                </Button>
            </div>
        </div>
    );
}

function ResultRow({
                       label,
                       value,
                   }: {
    label: string;
    value: string;
}) {
    return (
        <div className="flex flex-col gap-1 px-4 py-3 sm:flex-row sm:items-center sm:justify-between sm:gap-4">
            <span className="text-xs text-muted">
                {label}
            </span>

            <span className="break-all text-sm font-medium text-primary sm:text-right">
                {value}
            </span>
        </div>
    );
}