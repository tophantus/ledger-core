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

import type {AccountSummary} from "@/features/account/types/account";
import {useMyAccounts} from "@/features/account/hooks/use-my-accounts";

import {useWithdrawalIntents} from "../../hooks/use-withdrawal-intents";

import type {
    WithdrawalIntent,
    WithdrawalIntentFilters,
    WithdrawalIntentStatus,
} from "../../types/withdrawal";

import {WithdrawalListFilters} from "./withdrawal-list-filters";
import {WithdrawalListTable} from "./withdrawal-list-table";
import {WithdrawalListSkeleton} from "./withdrawal-list-skeleton";
import {WithdrawalListEmpty} from "./withdrawal-list-empty";
import {WithdrawalListPagination} from "./withdrawal-list-pagination";
import {AccountSummaryCardSkeleton} from "@/features/account/components/account-summary-card-skeleton";
import {AccountSummaryCard} from "@/features/account/components/account-summary-card";

const PAGE_SIZE = 20;

export default function WithdrawalListPage() {
    const t =
        useTranslations("withdrawal");

    const tErrors =
        useTranslations("errors");

    const router = useRouter();
    const searchParams = useSearchParams();

    const {
        getMyAccounts,
    } = useMyAccounts();

    const {
        getWithdrawalIntents,
    } = useWithdrawalIntents();

    const accountId =
        searchParams.get("accountId")
        ?? undefined;

    const status =
        (searchParams.get(
            "status",
        ) as WithdrawalIntentStatus)
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

    const filters =
        useMemo<WithdrawalIntentFilters>(
            () => ({
                accountId,
                status,
                page,
                size,
            }),
            [
                accountId,
                status,
                page,
                size,
            ],
        );

    const [accounts, setAccounts] =
        useState<AccountSummary[]>([]);

    const [withdrawals, setWithdrawals] =
        useState<WithdrawalIntent[]>([]);

    const [totalPages, setTotalPages] =
        useState(0);

    const [isAccountsLoading, setIsAccountsLoading] =
        useState(true);

    const [isLoading, setIsLoading] =
        useState(true);

    const [error, setError] =
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
                // the withdrawal list.
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

    /*
     * Load withdrawal intents whenever
     * URL filters change.
     */
    useEffect(() => {
        let mounted = true;

        const loadWithdrawals =
            async () => {
                try {
                    const response =
                        await getWithdrawalIntents(
                            filters,
                        );

                    if (!mounted) {
                        return;
                    }

                    if (!response.success) {
                        setError(
                            getErrorMessage(
                                response.code,
                            ),
                        );

                        return;
                    }

                    setWithdrawals(
                        response.data.content,
                    );

                    setTotalPages(
                        response.data.totalPages,
                    );

                    setError(null);
                } catch {
                    if (mounted) {
                        setError(
                            tErrors("fallback"),
                        );
                    }
                } finally {
                    if (mounted) {
                        setIsLoading(false);
                    }
                }
            };

        void loadWithdrawals();

        return () => {
            mounted = false;
        };
    }, [accountId, status, page, size, getWithdrawalIntents, getErrorMessage, tErrors, filters]);

    const handleFilterChange = (
        nextFilters: WithdrawalIntentFilters,
    ) => {
        const params =
            new URLSearchParams();

        if (nextFilters.accountId) {
            params.set(
                "accountId",
                nextFilters.accountId,
            );
        }

        if (nextFilters.status) {
            params.set(
                "status",
                nextFilters.status,
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

        setIsLoading(true);
        setError(null);

        router.replace(
            `${window.location.pathname}?${nextQuery}`,
        );
    };

    const handlePageChange = (
        nextPage: number,
    ) => {
        handleFilterChange({
            ...filters,
            page: nextPage,
        });
    };

    const handleCancelled = () => {
        setIsLoading(true);
        setError(null);

        void getWithdrawalIntents(filters)
            .then((response) => {
                if (!response.success) {
                    setError(
                        getErrorMessage(
                            response.code,
                        ),
                    );

                    return;
                }

                setWithdrawals(
                    response.data.content,
                );

                setTotalPages(
                    response.data.totalPages,
                );
            })
            .catch(() => {
                setError(
                    tErrors("fallback"),
                );
            })
            .finally(() => {
                setIsLoading(false);
            });
    };

    return (
        <section className="
            mx-auto
            w-full
            space-y-6
        ">
            <div>
                <h1 className="
                    text-2xl
                    font-semibold
                    text-primary
                ">
                    {accountId
                        ? t("list.accountTitle")
                        : t("list.title")}
                </h1>

                <p className="
                    mt-1
                    text-sm
                    text-muted
                ">
                    {accountId
                        ? t("list.accountDescription")
                        : t("list.description")}
                </p>
            </div>

            <WithdrawalListFilters
                accounts={accounts}
                isAccountsLoading={
                    isAccountsLoading
                }
                filters={filters}
                onChange={
                    handleFilterChange
                }
            />

            {accountId && (
                <>
                    {isAccountsLoading && (
                        <AccountSummaryCardSkeleton />
                    )}

                    {!isAccountsLoading &&
                        account && (
                            <AccountSummaryCard
                                account={account}
                            />
                        )}
                </>
            )}

            {isLoading ? (
                <WithdrawalListSkeleton />
            ) : error ? (
                <div className="
                    rounded-lg
                    border
                    border-danger
                    bg-surface
                    p-4
                ">
                    <p className="
                        text-sm
                        text-danger
                    ">
                        {error}
                    </p>
                </div>
            ) : withdrawals.length === 0 ? (
                <WithdrawalListEmpty />
            ) : (
                <>
                    <WithdrawalListTable
                        withdrawals={
                            withdrawals
                        }
                        onCancelled={
                            handleCancelled
                        }
                    />

                    {totalPages > 1 && (
                        <WithdrawalListPagination
                            page={page}
                            totalPages={
                                totalPages
                            }
                            onPageChange={
                                handlePageChange
                            }
                        />
                    )}
                </>
            )}
        </section>
    );
}