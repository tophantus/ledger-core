"use client";

import {useCallback, useEffect, useMemo, useState} from "react";
import {useTranslations} from "next-intl";

import {Link} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import {useAccountTransactions} from "../hooks/use-account-transactions";
import type {Transaction} from "../types/transaction";

import {TransactionRow} from "./transaction-row";
import {TransactionSkeleton} from "./transaction-skeleton";

interface RecentTransactionListProps {
    accountId: string;
}

export function RecentTransactionList({
                                          accountId,
                                      }: RecentTransactionListProps) {
    const t = useTranslations("transaction");
    const tErrors = useTranslations("errors");

    const {getAccountTransactions} =
        useAccountTransactions();

    const [transactions, setTransactions] =
        useState<Transaction[]>([]);

    const [isLoading, setIsLoading] =
        useState(true);

    const [errorMessage, setErrorMessage] =
        useState<string | null>(null);

    const getErrorMessage = useCallback((
        code?: string,
    ): string => {
        if (
            code &&
            tErrors.has(code)
        ) {
            return tErrors(code);
        }

        return tErrors("fallback");
    }, [tErrors]);

    useEffect(() => {
        let mounted = true;

        const loadTransactions = async () => {
            setIsLoading(true);
            setErrorMessage(null);

            try {
                const response =
                    await getAccountTransactions(
                        accountId,
                        {
                            page: 0,
                            size: 5,
                        },
                    );

                if (!mounted) {
                    return;
                }

                if (!response.success) {
                    setErrorMessage(
                        getErrorMessage(
                            response.code,
                        ),
                    );

                    return;
                }

                setTransactions(
                    response.data.content,
                );
            } catch {
                if (mounted) {
                    setErrorMessage(
                        tErrors("fallback"),
                    );
                }
            } finally {
                if (mounted) {
                    setIsLoading(false);
                }
            }
        };

        void loadTransactions();

        return () => {
            mounted = false;
        };
    }, [accountId, getAccountTransactions, getErrorMessage, tErrors]);

    const href = useMemo(() => {
        return `${ROUTES.TRANSACTION.LIST}?accountId=${accountId}`
    }, [accountId]);

    return (
        <section className="rounded-lg border border-border bg-surface">
            <div className="flex items-center justify-between border-b border-border px-6 py-4">
                <h2 className="text-lg font-semibold text-primary">
                    {t("recent.title")}
                </h2>

                <Link
                    href={href}
                    className="text-sm font-medium text-primary hover:underline"
                >
                    {t("recent.viewAll")}
                </Link>
            </div>

            <div className="px-6">
                {isLoading && (
                    <div className="divide-y divide-border">
                        {Array.from({length: 5}).map(
                            (_, index) => (
                                <TransactionSkeleton
                                    key={index}
                                />
                            ),
                        )}
                    </div>
                )}

                {!isLoading &&
                    errorMessage && (
                        <div className="py-8 text-center">
                            <p className="text-sm text-danger">
                                {errorMessage}
                            </p>
                        </div>
                    )}

                {!isLoading &&
                    !errorMessage &&
                    transactions.length === 0 && (
                        <div className="py-8 text-center">
                            <p className="text-sm text-muted">
                                {t("recent.empty")}
                            </p>
                        </div>
                    )}

                {!isLoading &&
                    !errorMessage &&
                    transactions.length > 0 && (
                        <div>
                            {transactions.map(
                                (transaction) => (
                                    <TransactionRow
                                        key={
                                            transaction.id
                                        }
                                        transaction={
                                            transaction
                                        }
                                    />
                                ),
                            )}
                        </div>
                    )}
            </div>
        </section>
    );
}