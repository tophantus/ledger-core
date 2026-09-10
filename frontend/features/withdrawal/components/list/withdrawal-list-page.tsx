"use client";

import {
    useEffect,
    useState,
} from "react";
import {useTranslations} from "next-intl";

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

const PAGE_SIZE = 20;

export default function WithdrawalListPage() {
    const t =
        useTranslations("withdrawal");

    const tErrors =
        useTranslations("errors");

    const {
        getWithdrawalIntents,
    } =
        useWithdrawalIntents();

    const [withdrawals, setWithdrawals] =
        useState<WithdrawalIntent[]>([]);

    const [page, setPage] =
        useState(0);

    const [totalPages, setTotalPages] =
        useState(0);

    const [status, setStatus] =
        useState<
            WithdrawalIntentStatus | undefined
        >();

    const [reloadKey, setReloadKey] =
        useState(0);

    const [isLoading, setIsLoading] =
        useState(true);

    const [error, setError] =
        useState<string | null>(null);

    useEffect(() => {
        let mounted = true;

        const loadWithdrawals =
            async () => {
                setIsLoading(true);
                setError(null);

                try {
                    const filters:
                        WithdrawalIntentFilters = {
                        status,
                        page,
                        size: PAGE_SIZE,
                    };

                    const response =
                        await getWithdrawalIntents(
                            filters,
                        );

                    if (!mounted) {
                        return;
                    }

                    if (!response.success) {
                        if (
                            response.code &&
                            tErrors.has(
                                response.code,
                            )
                        ) {
                            setError(
                                tErrors(
                                    response.code,
                                ),
                            );
                        } else {
                            setError(
                                tErrors(
                                    "fallback",
                                ),
                            );
                        }

                        return;
                    }

                    setWithdrawals(
                        response.data.content,
                    );

                    setTotalPages(
                        response.data.totalPages,
                    );
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
    }, [
        getWithdrawalIntents,
        page,
        status,
        tErrors,
        reloadKey,
    ]);

    const handleStatusChange = (
        value:
            | WithdrawalIntentStatus
            | undefined,
    ) => {
        setStatus(value);
        setPage(0);
    };

    const handlePageChange = (
        nextPage: number,
    ) => {
        setPage(nextPage);
    };

    const handleCancelled = () => {
        setReloadKey(
            (current) => current + 1,
        );
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
                    {t("list.title")}
                </h1>

                <p className="
                    mt-1
                    text-sm
                    text-muted
                ">
                    {t(
                        "list.description",
                    )}
                </p>
            </div>

            <WithdrawalListFilters
                status={status}
                onStatusChange={
                    handleStatusChange
                }
            />

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

                    <WithdrawalListPagination
                        page={page}
                        totalPages={
                            totalPages
                        }
                        onPageChange={
                            handlePageChange
                        }
                    />
                </>
            )}
        </section>
    );
}