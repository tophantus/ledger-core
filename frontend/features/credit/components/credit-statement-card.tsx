"use client";

import {useLocale, useTranslations} from "next-intl";

import {formatMoney} from "@/lib/utils/currency";

import type {CreditStatement} from "../types/credit-statement";
import {CreditStatementStatus} from "../types/credit-statement";
import { Currency } from "@/lib/constants/currency";

interface CreditStatementCardProps {
    statement: CreditStatement;
    currency: Currency;
}

function formatDate(
    value: string,
    locale: string,
) {
    return new Intl.DateTimeFormat(
        locale,
        {
            year: "numeric",
            month: "short",
            day: "numeric",
        },
    ).format(new Date(`${value}T00:00:00`));
}

function getStatusClassName(
    status: CreditStatementStatus,
) {
    switch (status) {
        case CreditStatementStatus.PAID:
            return "bg-success/10 text-success";

        case CreditStatementStatus.PARTIALLY_PAID:
            return "bg-warning/10 text-warning";

        case CreditStatementStatus.OVERDUE:
            return "bg-danger/10 text-danger";

        case CreditStatementStatus.ISSUED:
            return "bg-primary/10 text-primary";

        case CreditStatementStatus.NO_PAYMENT_DUE:
            return "bg-surface-subtle text-text-muted";

        case CreditStatementStatus.OPEN:
        default:
            return "bg-surface-subtle text-text-primary";
    }
}

export function CreditStatementCard(
    {
        statement,
        currency
    }: CreditStatementCardProps
) {
    const t = useTranslations("credit");
    const locale = useLocale();

    return (
        <article
            className="
                rounded-xl border border-border
                bg-surface p-4 transition
                hover:border-primary/30
                sm:p-5
            "
        >
            <div
                className="
                    flex flex-col gap-3
                    sm:flex-row sm:items-start
                    sm:justify-between
                "
            >
                <div>
                    <p className="text-sm font-semibold text-text-primary">
                        {formatDate(
                            statement.periodStart,
                            locale,
                        )}{" "}
                        –{" "}
                        {formatDate(
                            statement.periodEnd,
                            locale,
                        )}
                    </p>

                    <p className="mt-1 text-xs text-text-muted">
                        {t(
                            "statements.statementDate",
                        )}{" "}
                        {formatDate(
                            statement.statementDate,
                            locale,
                        )}
                    </p>
                </div>

                <span
                    className={`
                        inline-flex w-fit rounded-full
                        px-2.5 py-1 text-xs font-medium
                        ${getStatusClassName(
                        statement.status,
                    )}
                    `}
                >
                    {t(
                        `statements.statuses.${statement.status}`,
                    )}
                </span>
            </div>

            <div
                className="
                    mt-4 grid gap-4
                    sm:grid-cols-2
                    lg:grid-cols-4
                "
            >
                <StatementAmount
                    label={t(
                        "statements.openingBalance",
                    )}
                    value={formatMoney(
                        statement.openingBalance,
                        currency,
                        locale,
                    )}
                />

                <StatementAmount
                    label={t(
                        "statements.purchasesAmount",
                    )}
                    value={formatMoney(
                        statement.purchasesAmount,
                        currency,
                        locale,
                    )}
                />

                <StatementAmount
                    label={t(
                        "statements.paymentsAmount",
                    )}
                    value={formatMoney(
                        statement.paymentsAmount,
                        currency,
                        locale,
                    )}
                />

                <StatementAmount
                    label={t(
                        "statements.closingBalance",
                    )}
                    value={formatMoney(
                        statement.closingBalance,
                        currency,
                        locale,
                    )}
                    emphasized
                />
            </div>

            <div
                className="
                    mt-4 grid gap-4 border-t
                    border-border pt-4
                    sm:grid-cols-2
                    lg:grid-cols-4
                "
            >
                <StatementAmount
                    label={t(
                        "statements.minimumPayment",
                    )}
                    value={formatMoney(
                        statement.minimumPayment,
                        currency,
                        locale,
                    )}
                />

                <StatementAmount
                    label={t(
                        "statements.paidAmount",
                    )}
                    value={formatMoney(
                        statement.paidAmount,
                        currency,
                        locale,
                    )}
                />

                <StatementAmount
                    label={t(
                        "statements.feesAmount",
                    )}
                    value={formatMoney(
                        statement.feesAmount,
                        currency,
                        locale,
                    )}
                />

                <StatementAmount
                    label={t(
                        "statements.postedInterestAmount",
                    )}
                    value={formatMoney(
                        statement.postedInterestAmount,
                        currency,
                        locale,
                    )}
                />

                <StatementAmount
                    label={t("statements.interestAmount")}
                    value={formatMoney(
                        statement.interestAmount,
                        currency,
                        locale,
                    )}
                    badge={
                        statement.status === CreditStatementStatus.PAID
                            ? {
                                label: t(
                                    "statements.interestWaived",
                                ),
                                className:
                                    "bg-success/10 text-success",
                            }
                            : undefined
                    }
                />
            </div>

            <div
                className="
                    mt-4 border-t border-border
                    pt-4 text-xs text-text-muted
                "
            >
                {t("statements.dueDate")}:{" "}
                <span className="font-medium text-text-primary">
                    {formatDate(
                        statement.dueDate,
                        locale,
                    )}
                </span>
            </div>
        </article>
    );
}

interface StatementAmountProps {
    label: string;
    value: string;
    emphasized?: boolean;
    badge?: {
        label: string;
        className: string;
    };
}

function StatementAmount({
                             label,
                             value,
                             emphasized = false,
                             badge,
                         }: StatementAmountProps) {
    return (
        <div>
            <p className="text-xs text-text-muted">
                {label}
            </p>

            <div className="mt-1 flex flex-wrap items-center gap-2">
                <p
                    className={`
                        text-sm
                        ${
                        emphasized
                            ? "font-semibold text-text-primary"
                            : "font-medium text-text-primary"
                    }
                    `}
                >
                    {value}
                </p>

                {badge && (
                    <span
                        className={`
                            inline-flex rounded-full
                            px-2 py-0.5 text-[11px]
                            font-medium
                            ${badge.className}
                        `}
                    >
                        {badge.label}
                    </span>
                )}
            </div>
        </div>
    );
}