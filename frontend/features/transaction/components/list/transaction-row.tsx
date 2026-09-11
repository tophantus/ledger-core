"use client";

import {
    ArrowDownLeft,
    ArrowUpRight, RefreshCcw,
} from "lucide-react";
import {useLocale, useTranslations} from "next-intl";

import type {Transaction} from "../../types/transaction";
import {Link} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import {formatMoney} from "@/lib/utils/currency";

interface TransactionRowProps {
    transaction: Transaction;
}

function formatAmount(
    amount: string,
    incoming: boolean | null,
): string {
    if (incoming === true) {
        return `+ ${amount}`;
    }

    if (incoming === false) {
        return `- ${amount}`;
    }

    return amount;
}

function formatDate(value: string): string {
    return new Date(value).toLocaleString();
}

export function TransactionRow({
                                   transaction,
                               }: TransactionRowProps) {
    const t = useTranslations("transaction");

    const locale = useLocale();

    const isIncoming = transaction.incoming !== null && transaction.incoming;
    const isOutgoing = transaction.incoming !== null && !transaction.incoming;

    const amount = formatMoney(
        transaction.amount,
        transaction.currency,
        locale)

    return (
        <Link
            href={ROUTES.TRANSACTION.DETAIL(
                transaction.id,
            )}
            className="flex gap-4 border-b border-border py-5"
        >
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-background-subtle">
                {isIncoming ? (
                    <ArrowDownLeft className="h-5 w-5 text-success" />
                ) : isOutgoing ? (
                    <ArrowUpRight className="h-5 w-5 text-danger" />
                ) : <RefreshCcw className={"h-5 w-5 text-foreground"} />}
            </div>

            <div className="min-w-0 flex-1">
                <div className="flex flex-col justify-between gap-2 sm:flex-row">
                    <div className="min-w-0">
                        <p className="truncate text-sm font-medium text-primary">
                            {t(`types.${transaction.type}`)}
                        </p>

                        <p className="truncate text-xs text-muted">
                            {transaction.reference}
                        </p>

                        {transaction.description && (
                            <p className="mt-1 truncate text-xs text-muted">
                                {transaction.description}
                            </p>
                        )}
                    </div>

                    <div className="shrink-0 text-left sm:text-right">
                        <p
                            className={`text-sm font-semibold ${
                                isIncoming
                                    ? "text-success"
                                    : isOutgoing
                                        ? "text-danger"
                                        : "text-foreground"
                            }`}
                        >
                            {formatAmount(
                                amount,
                                transaction.incoming,
                            )}
                        </p>

                        <p className="text-xs text-muted">
                            {t(
                                `statuses.${transaction.status}`,
                            )}
                        </p>
                    </div>
                </div>

                <p className="mt-2 text-xs text-muted">
                    {formatDate(transaction.createdAt)}
                </p>
            </div>
        </Link>
    );
}