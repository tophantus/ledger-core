"use client";

import {useEffect, useState} from "react";
import {useSearchParams} from "next/navigation";
import {useTranslations} from "next-intl";
import {ArrowLeft} from "lucide-react";

import {Link} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import {AccountSummary} from "@/features/account/components/account-summary";
import {AccountSummarySkeleton} from "@/features/account/components/account-summary-skeleton";
import {useAccount} from "@/features/account/hooks/use-account";

import {TransactionFilter} from "@/features/transaction/components/transaction-filter";
import {TransactionList} from "@/features/transaction/components/transaction-list";
import {TransactionPagination} from "@/features/transaction/components/transaction-pagination";
import {useAccountTransactions} from "@/features/transaction/hooks/use-account-transactions";

import type {Account} from "@/features/account/types/account";
import type {
    PageResponse,
    Transaction,
    TransactionFilters,
} from "@/features/transaction/types/transaction";

export default function TransactionsPage() {
    const t = useTranslations("transaction");
    const searchParams = useSearchParams();

    const accountId = searchParams.get("accountId");

    const {getAccount} = useAccount();
    const {getAccountTransactions} =
        useAccountTransactions();

    const [account, setAccount] =
        useState<Account | null>(null);

    const [filters, setFilters] =
        useState<TransactionFilters>({
            page: 0,
            size: 20,
        });

    const [result, setResult] =
        useState<PageResponse<Transaction> | null>(
            null,
        );

    const [isAccountLoading, setIsAccountLoading] =
        useState(true);

    const [isTransactionLoading, setIsTransactionLoading] =
        useState(true);

    const [hasAccountError, setHasAccountError] =
        useState(false);

    const [hasTransactionError, setHasTransactionError] =
        useState(false);

    useEffect(() => {
        if (!accountId) {
            return;
        }

        let mounted = true;

        const loadAccount = async () => {
            setIsAccountLoading(true);
            setHasAccountError(false);

            try {
                const response =
                    await getAccount(accountId);

                if (!mounted) {
                    return;
                }

                if (!response.success) {
                    setHasAccountError(true);
                    return;
                }

                setAccount(response.data);
            } catch {
                if (mounted) {
                    setHasAccountError(true);
                }
            } finally {
                if (mounted) {
                    setIsAccountLoading(false);
                }
            }
        };

        void loadAccount();

        return () => {
            mounted = false;
        };
    }, [accountId, getAccount]);

    useEffect(() => {
        if (!accountId) {
            return;
        }

        let mounted = true;

        const loadTransactions = async () => {
            setIsTransactionLoading(true);
            setHasTransactionError(false);

            try {
                const response =
                    await getAccountTransactions(
                        accountId,
                        filters,
                    );

                if (!mounted) {
                    return;
                }

                if (!response.success) {
                    setHasTransactionError(true);
                    return;
                }

                setResult(response.data);
            } catch {
                if (mounted) {
                    setHasTransactionError(true);
                }
            } finally {
                if (mounted) {
                    setIsTransactionLoading(false);
                }
            }
        };

        void loadTransactions();

        return () => {
            mounted = false;
        };
    }, [
        accountId,
        filters,
        getAccountTransactions,
    ]);

    const handleFilterChange = (
        nextFilters: TransactionFilters,
    ) => {
        setFilters({
            ...nextFilters,
            page: 0,
            size: filters.size ?? 20,
        });
    };

    const handlePageChange = (page: number) => {
        setFilters((current) => ({
            ...current,
            page,
        }));
    };

    if (!accountId) {
        return (
            <section className="space-y-6">
                <Link
                    href={ROUTES.DASHBOARD}
                    className="
                        inline-flex
                        items-center
                        gap-2
                        text-sm
                        text-text-muted
                        hover:text-text-primary
                    "
                >
                    <ArrowLeft className="h-4 w-4" />
                    {t("back")}
                </Link>

                <div className="rounded-lg border border-border bg-surface p-8 text-center">
                    <p className="text-sm text-text-muted">
                        {t("invalidAccount")}
                    </p>
                </div>
            </section>
        );
    }

    return (
        <section className="space-y-6">
            <div className="space-y-2">
                <Link
                    href={ROUTES.ACCOUNT.DETAIL(accountId)}
                    className="
                        inline-flex
                        items-center
                        gap-2
                        text-sm
                        text-text-muted
                        hover:text-text-primary
                    "
                >
                    <ArrowLeft className="h-4 w-4" />
                    {t("back")}
                </Link>

                <div>
                    <h1 className="text-2xl font-semibold text-text-primary">
                        {t("title")}
                    </h1>

                    <p className="mt-1 text-sm text-text-muted">
                        {t("accountTransactions")}
                    </p>
                </div>
            </div>

            {isAccountLoading && (
                <AccountSummarySkeleton />
            )}

            {!isAccountLoading &&
                !hasAccountError &&
                account && (
                    <AccountSummary
                        account={account}
                    />
                )}

            {!isAccountLoading &&
                hasAccountError && (
                    <div className="rounded-lg border border-border bg-surface p-6">
                        <p className="text-sm text-text-muted">
                            {t("accountLoadError")}
                        </p>
                    </div>
                )}

            <TransactionFilter
                filters={filters}
                onChange={handleFilterChange}
            />

            <TransactionList
                transactions={result?.content ?? []}
                isLoading={isTransactionLoading}
                hasError={hasTransactionError}
            />

            {!isTransactionLoading &&
                !hasTransactionError &&
                result &&
                result.totalPages > 1 && (
                    <TransactionPagination
                        page={result.page}
                        totalPages={result.totalPages}
                        onPageChange={handlePageChange}
                    />
                )}
        </section>
    );
}