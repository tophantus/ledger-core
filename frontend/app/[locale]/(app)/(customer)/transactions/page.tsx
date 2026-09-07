"use client";

import {useCallback, useEffect, useState} from "react";
import {useSearchParams} from "next/navigation";
import {useTranslations} from "next-intl";
import {ArrowLeft} from "lucide-react";

import {Link} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import {AccountSummary} from "@/features/account/components/account-summary";
import {AccountSummarySkeleton} from "@/features/account/components/account-summary-skeleton";
import {useAccount} from "@/features/account/hooks/use-account";
import type {Account} from "@/features/account/types/account";

import {TransactionFilter} from "@/features/transaction/components/transaction-filter";
import {TransactionList} from "@/features/transaction/components/transaction-list";
import {TransactionPagination} from "@/features/transaction/components/transaction-pagination";
import {useAccountTransactions} from "@/features/transaction/hooks/use-account-transactions";
import {useUserTransactions} from "@/features/transaction/hooks/use-user-transactions";

import type {
    Transaction,
    TransactionFilters,
} from "@/features/transaction/types/transaction";
import {PageResponse} from "@/lib/api/types";

export default function TransactionsPage() {
    const t = useTranslations("transaction");
    const tErrors = useTranslations("errors");

    const searchParams = useSearchParams();

    const accountId = searchParams.get("accountId");

    const {getAccount} = useAccount();
    const {
        getAccountTransactions,
    } = useAccountTransactions();
    const {
        getUserTransactions,
    } = useUserTransactions();

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
        useState(false);

    const [isTransactionLoading, setIsTransactionLoading] =
        useState(true);

    const [accountError, setAccountError] =
        useState<string | null>(null);

    const [
        transactionError,
        setTransactionError,
    ] = useState<string | null>(null);


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

    /*
     * Load account information only when
     * this page is opened for a specific account.
     */
    useEffect(() => {
        if (!accountId) {
            return;
        }

        let mounted = true;

        const loadAccount = async () => {
            setIsAccountLoading(true);
            setAccountError(null);

            try {
                const response =
                    await getAccount(accountId);

                if (!mounted) {
                    return;
                }

                if (!response.success) {
                    setAccountError(
                        getErrorMessage(
                            response.code,
                        ),
                    );

                    return;
                }

                setAccount(response.data);
            } catch {
                if (mounted) {
                    setAccountError(
                        tErrors("fallback"),
                    );
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
    }, [accountId, getAccount, getErrorMessage, tErrors]);

    /*
     * Load transactions.
     *
     * accountId exists:
     *     GET /transactions/accounts/{accountId}/transactions
     *
     * accountId does not exist:
     *     GET /transactions
     */
    useEffect(() => {
        let mounted = true;

        const loadTransactions = async () => {
            setIsTransactionLoading(true);
            setTransactionError(null);

            try {
                const response = accountId
                    ? await getAccountTransactions(
                        accountId,
                        filters,
                    )
                    : await getUserTransactions(
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
            } catch {
                if (mounted) {
                    setTransactionError(
                        tErrors("fallback"),
                    );
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
    }, [accountId, filters, getAccountTransactions, getErrorMessage, getUserTransactions, tErrors]);

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

    const backHref = accountId
        ? ROUTES.ACCOUNT.DETAIL(accountId)
        : ROUTES.DASHBOARD;

    return (
        <section className="space-y-6">
            <div className="space-y-2">
                <Link
                    href={backHref}
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
                        {accountId
                            ? t("accountTitle")
                            : t("title")}
                    </h1>

                    <p className="mt-1 text-sm text-text-muted">
                        {accountId
                            ? t("accountTransactions")
                            : t("userTransactions")}
                    </p>
                </div>
            </div>

            {accountId && (
                <>
                    {isAccountLoading && (
                        <AccountSummarySkeleton />
                    )}

                    {!isAccountLoading &&
                        !accountError &&
                        account && (
                            <AccountSummary
                                account={account}
                            />
                        )}

                    {!isAccountLoading &&
                        accountError && (
                            <div
                                className="
                                    rounded-lg
                                    border
                                    border-border
                                    bg-surface
                                    p-6
                                "
                            >
                                <p className="text-sm text-danger">
                                    {accountError}
                                </p>
                            </div>
                        )}
                </>
            )}

            <TransactionFilter
                filters={filters}
                onChange={handleFilterChange}
            />

            <TransactionList
                transactions={
                    result?.content ?? []
                }
                isLoading={isTransactionLoading}
                hasError={
                    transactionError !== null
                }
            />

            {transactionError && (
                <div
                    className="
                        rounded-lg
                        border
                        border-border
                        bg-surface
                        p-6
                    "
                >
                    <p className="text-sm text-danger">
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