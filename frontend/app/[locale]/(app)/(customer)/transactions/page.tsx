"use client";

import {
    useCallback,
    useEffect, useMemo,
    useState,
} from "react";
import {ArrowLeft} from "lucide-react";
import {
    useRouter,
    useSearchParams,
} from "next/navigation";
import {useTranslations} from "next-intl";

import type {PageResponse} from "@/lib/api/types";
import {Link} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import {AccountSummary as AccountSummaryComponent} from "@/features/account/components/account-summary";
import {AccountSummarySkeleton} from "@/features/account/components/account-summary-skeleton";
import {useAccount} from "@/features/account/hooks/use-account";
import type {Account, AccountSummary} from "@/features/account/types/account";

import {useMyAccounts} from "@/features/account/hooks/use-my-accounts";

import {TransactionFilter} from "@/features/transaction/components/transaction-filter";
import {TransactionList} from "@/features/transaction/components/transaction-list";
import {TransactionPagination} from "@/features/transaction/components/transaction-pagination";
import {useTransactions} from "@/features/transaction/hooks/use-transactions";
import type {
    Transaction,
    TransactionFilters,
} from "@/features/transaction/types/transaction";

const PAGE_SIZE = 20;

export default function TransactionsPage() {
    const t = useTranslations("transaction");
    const tErrors = useTranslations("errors");

    const router = useRouter();
    const searchParams = useSearchParams();

    const {getAccount} = useAccount();
    const {getMyAccounts} = useMyAccounts();
    const {getTransactions} = useTransactions();

    const accountId =
        searchParams.get("accountId")
        ?? undefined;

    const filters: TransactionFilters = useMemo(
        () => ({
            accountId,
            type:
                (searchParams.get(
                    "type",
                ) as TransactionFilters["type"])
                ?? undefined,
            status:
                (searchParams.get(
                    "status",
                ) as TransactionFilters["status"])
                ?? undefined,
            currency:
                searchParams.get("currency")
                ?? undefined,
            from:
                searchParams.get("from")
                ?? undefined,
            to:
                searchParams.get("to")
                ?? undefined,
            page:
                Number(
                    searchParams.get("page") ?? "0",
                ) || 0,
            size:
                Number(
                    searchParams.get("size")
                    ?? PAGE_SIZE,
                ) || PAGE_SIZE,
        }),
        [searchParams, accountId],
    );

    const [accounts, setAccounts] =
        useState<AccountSummary[]>([]);

    const [account, setAccount] =
        useState<Account | null>(null);

    const [result, setResult] =
        useState<PageResponse<Transaction> | null>(
            null,
        );

    const [isAccountsLoading, setIsAccountsLoading] =
        useState(true);

    const [isAccountLoading, setIsAccountLoading] =
        useState(false);

    const [isTransactionLoading, setIsTransactionLoading] =
        useState(true);

    const [accountError, setAccountError] =
        useState<string | null>(null);

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

    /*
     * Load account information when
     * an account is selected.
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
    }, [
        accountId,
        getAccount,
        getErrorMessage,
        tErrors,
    ]);

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

        setIsTransactionLoading(true);
        setTransactionError(null);

        router.replace(
            `${ROUTES.TRANSACTION.LIST}?${params.toString()}`,
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
                    <h1 className="
                        text-2xl
                        font-semibold
                        text-text-primary
                    ">
                        {accountId
                            ? t("accountTitle")
                            : t("title")}
                    </h1>

                    <p className="
                        mt-1
                        text-sm
                        text-text-muted
                    ">
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
                            <AccountSummaryComponent
                                account={account}
                            />
                        )}

                    {!isAccountLoading &&
                        accountError && (
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
                                    {accountError}
                                </p>
                            </div>
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