"use client";

import {useEffect, useState} from "react";
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

    const {getAccountTransactions} =
        useAccountTransactions();

    const [transactions, setTransactions] =
        useState<Transaction[]>([]);

    const [isLoading, setIsLoading] =
        useState(true);

    const [hasError, setHasError] =
        useState(false);

    useEffect(() => {
        let mounted = true;

        const loadTransactions = async () => {
            setIsLoading(true);
            setHasError(false);

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
                    setHasError(true);
                    return;
                }

                setTransactions(
                    response.data.content,
                );
            } catch {
                if (mounted) {
                    setHasError(true);
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
    }, [
        accountId,
        getAccountTransactions,
    ]);

    return (
        <section className="rounded-lg border border-border bg-surface">
            <div className="flex items-center justify-between border-b border-border px-6 py-4">
                <h2 className="text-lg font-semibold text-text-primary">
                    {t("recent.title")}
                </h2>

                <Link
                    href={`${ROUTES.TRANSACTION.LIST}?accountId=${accountId}`}
                    className="text-sm font-medium text-text-primary hover:underline"
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

                {!isLoading && hasError && (
                    <div className="py-8 text-center">
                        <p className="text-sm text-text-muted">
                            {t("recent.loadError")}
                        </p>
                    </div>
                )}

                {!isLoading &&
                    !hasError &&
                    transactions.length === 0 && (
                        <div className="py-8 text-center">
                            <p className="text-sm text-text-muted">
                                {t("recent.empty")}
                            </p>
                        </div>
                    )}

                {!isLoading &&
                    !hasError &&
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