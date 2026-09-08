"use client";

import {ArrowLeft} from "lucide-react";
import {useLocale, useTranslations} from "next-intl";

import {Link} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import type {Transaction} from "../types/transaction";

import {formatMoney} from "@/lib/utils/currency";

interface TransactionDetailProps {
    transaction: Transaction;
}

export function TransactionDetail({
                                      transaction,
                                  }: TransactionDetailProps) {
    const t = useTranslations("transaction");

    const locale = useLocale();

    const typeLabel = t(
        `types.${transaction.type}`,
    );

    const statusLabel = t(
        `statuses.${transaction.status}`,
    );

    return (
        <section className="space-y-6">
            <Link
                href={ROUTES.TRANSACTION.LIST}
                className="
                    inline-flex
                    items-center
                    gap-2
                    text-sm
                    text-muted
                    hover:text-primary
                "
            >
                <ArrowLeft className="h-4 w-4" />
                {t("back")}
            </Link>

            <div>
                <h1 className="text-2xl font-semibold text-primary">
                    {t("detail.title")}
                </h1>

                <p className="mt-1 text-sm text-muted">
                    {t("detail.description")}
                </p>
            </div>

            <div className="rounded-lg border border-border bg-surface">
                <div className="border-b border-border px-6 py-5">
                    <p className="text-xs text-muted">
                        {t("detail.reference")}
                    </p>

                    <p className="mt-1 break-all text-lg font-semibold text-primary">
                        {transaction.reference}
                    </p>
                </div>

                <div className="divide-y divide-border">
                    <DetailRow
                        label={t("detail.type")}
                        value={typeLabel}
                    />

                    <DetailRow
                        label={t("detail.status")}
                        value={statusLabel}
                    />

                    <DetailRow
                        label={t("detail.amount")}
                        value={
                            formatMoney(
                                transaction.amount,
                                transaction.currency,
                                locale)}
                    />

                    <DetailRow
                        label={t("detail.sourceAccount")}
                        value={
                            transaction.sourceAccountId ??
                            "-"
                        }
                    />

                    <DetailRow
                        label={t("detail.destinationAccount")}
                        value={
                            transaction.destinationAccountId ??
                            "-"
                        }
                    />

                    <DetailRow
                        label={t("detail.descriptionField")}
                        value={
                            transaction.description ??
                            "-"
                        }
                    />

                    <DetailRow
                        label={t("detail.createdAt")}
                        value={formatDate(
                            transaction.createdAt,
                        )}
                    />

                    <DetailRow
                        label={t("detail.completedAt")}
                        value={
                            transaction.completedAt
                                ? formatDate(
                                    transaction.completedAt,
                                )
                                : "-"
                        }
                    />

                    <DetailRow
                        label={t("detail.transactionId")}
                        value={transaction.id}
                    />
                </div>
            </div>
        </section>
    );
}

function DetailRow({
                       label,
                       value,
                   }: {
    label: string;
    value: string;
}) {
    return (
        <div className="flex flex-col gap-1 px-6 py-4 sm:flex-row sm:items-center sm:justify-between sm:gap-6">
            <span className="text-sm text-muted">
                {label}
            </span>

            <span className="break-all text-sm font-medium text-primary sm:text-right">
                {value}
            </span>
        </div>
    );
}

function formatDate(value: string): string {
    return new Intl.DateTimeFormat(
        undefined,
        {
            dateStyle: "medium",
            timeStyle: "short",
        },
    ).format(new Date(value));
}