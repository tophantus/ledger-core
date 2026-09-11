"use client";

import {
    useCallback,
    useEffect, useMemo,
    useState,
} from "react";
import {
    useRouter,
    useSearchParams,
} from "next/navigation";
import {useTranslations} from "next-intl";

import type {PageResponse} from "@/lib/api/types";
import {ROUTES} from "@/lib/constants/routes";

import {
    AccountSummaryCard
} from "@/features/account/components/account-summary-card";
import {AccountSummaryCardSkeleton} from "@/features/account/components/account-summary-card-skeleton";
import type {AccountSummary} from "@/features/account/types/account";

import {useMyAccounts} from "@/features/account/hooks/use-my-accounts";

import {TransactionFilter} from "@/features/transaction/components/list/transaction-filter";
import {TransactionList} from "@/features/transaction/components/list/transaction-list";
import {TransactionPagination} from "@/features/transaction/components/list/transaction-pagination";
import {useTransactions} from "@/features/transaction/hooks/use-transactions";
import type {
    Transaction,
    TransactionFilters,
} from "@/features/transaction/types/transaction";

const PAGE_SIZE = 20;

export default function TransactionListPage() {
    const t = useTranslations("transaction");
    const tErrors = useTranslations("errors");

    const router = useRouter();
    const searchParams = useSearchParams();

    const {getMyAccounts} = useMyAccounts();
    const {getTransactions} = useTransactions();

    const accountId =
        searchParams.get("accountId")
        ?? undefined;

    const type =
        (searchParams.get(
            "type",
        ) as TransactionFilters["type"])
        ?? undefined;

    const status =
        (searchParams.get(
            "status",
        ) as TransactionFilters["status"])
        ?? undefined;

    const currency =
        searchParams.get("currency")
        ?? undefined;

    const from =
        searchParams.get("from")
        ?? undefined;

    const to =
        searchParams.get("to")
        ?? undefined;

    const page =
        Number(
            searchParams.get("page") ?? "0",
        ) || 0;

    const size =
        Number(
            searchParams.get("size")
            ?? PAGE_SIZE,
        ) || PAGE_SIZE;

    const filters: TransactionFilters =
        useMemo(
            () => ({
                accountId,
                type,
                status,
                currency,
                from,
                to,
                page,
                size,
            }),
            [
                accountId,
                type,
                status,
                currency,
                from,
                to,
                page,
                size,
            ],
        );

    const [accounts, setAccounts] =
        useState<AccountSummary[]>([]);

    const [result, setResult] =
        useState<PageResponse<Transaction> | null>(
            null,
        );

    const [isAccountsLoading, setIsAccountsLoading] =
        useState(true);

    const [isTransactionLoading, setIsTransactionLoading] =
        useState(true);

    const [transactionError, setTransactionError] =
        useState<string | null>(null);

    const getErrorMessage = useCallback(
        (code?: string): string => {
            if (
                code &&
                tErrors.has(code)
            ) {
                return tErrors(code);
            }

            return tErrors("fallback");
        },
        [tErrors],
    );

    /*
     * Load accounts for the account filter.
     */
    useEffect(() => {
        let mounted = true;

        const loadAccounts = async () => {
            try {
                const response =
                    await getMyAccounts();

                if (!mounted) {
                    return;
                }

                if (!response.success) {
                    return;
                }

                setAccounts(response.data);
            } catch {
                // Account filter does not block
                // the transaction page.
            } finally {
                if (mounted) {
                    setIsAccountsLoading(false);
                }
            }
        };

        void loadAccounts();

        return () => {
            mounted = false;
        };
    }, [getMyAccounts]);


    const account = useMemo(
        () =>
            accountId
                ? accounts.find(
                (item) =>
                    item.id === accountId,
            ) ?? null
                : null,
        [accounts, accountId],
    );

    /*
     * Load transactions whenever
     * URL filters change.
     */
    useEffect(() => {
        let mounted = true;

        const loadTransactions = async () => {
            try {
                const response =
                    await getTransactions(
                        filters,
                    );

                if (!mounted) {
                    return;
                }

                if (!response.success) {
                    setTransactionError(
                        getErrorMessage(
                            response.code,
                        ),
                    );

                    return;
                }

                setResult(response.data);
                setTransactionError(null);
            } catch {
                if (mounted) {
                    setTransactionError(
                        tErrors("fallback"),
                    );
                }
            } finally {
                if (mounted) {
                    setIsTransactionLoading(
                        false,
                    );
                }
            }
        };

        void loadTransactions();

        return () => {
            mounted = false;
        };
    }, [accountId, filters.type, filters.status, filters.currency, filters.from, filters.to, filters.page, filters.size, getTransactions, getErrorMessage, tErrors, filters]);

    const handleFilterChange = (
        nextFilters: TransactionFilters,
    ) => {
        const params =
            new URLSearchParams();

        if (nextFilters.accountId) {
            params.set(
                "accountId",
                nextFilters.accountId,
            );
        }

        if (nextFilters.type) {
            params.set(
                "type",
                nextFilters.type,
            );
        }

        if (nextFilters.status) {
            params.set(
                "status",
                nextFilters.status,
            );
        }

        if (nextFilters.currency) {
            params.set(
                "currency",
                nextFilters.currency,
            );
        }

        if (nextFilters.from) {
            params.set(
                "from",
                nextFilters.from,
            );
        }

        if (nextFilters.to) {
            params.set(
                "to",
                nextFilters.to,
            );
        }

        params.set(
            "page",
            String(nextFilters.page ?? 0),
        );

        params.set(
            "size",
            String(
                nextFilters.size ?? PAGE_SIZE,
            ),
        );

        const nextQuery =
            params.toString();

        const currentQuery =
            searchParams.toString();

        if (nextQuery === currentQuery) {
            return;
        }

        setIsTransactionLoading(true);
        setTransactionError(null);

        router.replace(
            `${ROUTES.TRANSACTION.LIST}?${nextQuery}`,
        );
    };

    const handlePageChange = (
        page: number,
    ) => {
        handleFilterChange({
            ...filters,
            page,
        });
    };

    return (
        <section className="space-y-6">
            <div className="space-y-2">
                <div>
                    <h1 className="
                        text-2xl
                        font-semibold
                        text-primary
                    ">
                        {accountId
                            ? t("accountTitle")
                            : t("title")}
                    </h1>

                    <p className="
                        mt-1
                        text-sm
                        text-muted
                    ">
                        {accountId
                            ? t("accountTransactions")
                            : t("userTransactions")}
                    </p>
                </div>
            </div>

            {accountId && (
                <>
                    {isAccountsLoading && (
                        <AccountSummaryCardSkeleton />
                    )}

                    {!isAccountsLoading && account && (
                        <AccountSummaryCard
                            account={account}
                        />
                    )}
                </>
            )}

            <TransactionFilter
                accounts={accounts}
                isAccountsLoading={
                    isAccountsLoading
                }
                filters={filters}
                onChange={handleFilterChange}
            />

            <TransactionList
                transactions={
                    result?.content ?? []
                }
                isLoading={
                    isTransactionLoading
                }
                hasError={
                    transactionError !== null
                }
            />

            {transactionError && (
                <div className="
                    rounded-lg
                    border
                    border-border
                    bg-surface
                    p-6
                ">
                    <p className="
                        text-sm
                        text-danger
                    ">
                        {transactionError}
                    </p>
                </div>
            )}

            {!isTransactionLoading &&
                !transactionError &&
                result &&
                result.totalPages > 1 && (
                    <TransactionPagination
                        page={result.page}
                        totalPages={
                            result.totalPages
                        }
                        onPageChange={
                            handlePageChange
                        }
                    />
                )}
        </section>
    );
}