"use client";

import {
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import {
    useRouter,
    useSearchParams,
} from "next/navigation";
import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";
import type {
    AccountSummary,
} from "@/features/account/types/account";
import {useMyAccounts} from "@/features/account/hooks/use-my-accounts";
import {AccountSummaryCard} from "@/features/account/components/account-summary-card";
import {AccountSummaryCardSkeleton} from "@/features/account/components/account-summary-card-skeleton";

import {useWebhooks} from "../../hooks/use-webhooks";

import type {
    Webhook,
    WebhookFilters,
} from "../../types/webhook";

import {WebhookListFilters} from "./webhook-list-filters";
import {WebhookListTable} from "./webhook-list-table";
import {WebhookListSkeleton} from "./webhook-list-skeleton";
import {WebhookListEmpty} from "./webhook-list-empty";
import {WebhookListPagination} from "./webhook-list-pagination";
import {RegisterWebhookModal} from "../register-webhook-modal";

const PAGE_SIZE = 20;

export default function WebhookListPage() {
    const t =
        useTranslations("webhook");

    const tErrors =
        useTranslations("errors");

    const router =
        useRouter();

    const searchParams =
        useSearchParams();

    const {
        getMyAccounts,
    } = useMyAccounts();

    const {
        getWebhooks,
    } = useWebhooks();

    const accountId =
        searchParams.get("accountId")
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
        useMemo<WebhookFilters>(
            () => ({
                accountId,
                page,
                size,
            }),
            [
                accountId,
                page,
                size,
            ],
        );

    const [accounts, setAccounts] =
        useState<AccountSummary[]>([]);

    const [webhooks, setWebhooks] =
        useState<Webhook[]>([]);

    const [totalPages, setTotalPages] =
        useState(0);

    const [
        isAccountsLoading,
        setIsAccountsLoading,
    ] = useState(true);

    const [
        isLoading,
        setIsLoading,
    ] = useState(true);

    const [
        isRegisterModalOpen,
        setIsRegisterModalOpen,
    ] = useState(false);

    const [error, setError] =
        useState<string | null>(null);

    const getErrorMessage =
        useCallback(
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

    const account =
        useMemo(
            () =>
                accountId
                    ? accounts.find(
                    (item) =>
                        item.id ===
                        accountId,
                ) ?? null
                    : null,
            [
                accounts,
                accountId,
            ],
        );

    useEffect(() => {
        let mounted = true;

        const loadAccounts =
            async () => {
                try {
                    const response =
                        await getMyAccounts();

                    if (!mounted) {
                        return;
                    }

                    if (!response.success) {
                        return;
                    }

                    setAccounts(
                        response.data,
                    );
                } catch {
                    // Account filter does not
                    // block the webhook list.
                } finally {
                    if (mounted) {
                        setIsAccountsLoading(
                            false,
                        );
                    }
                }
            };

        void loadAccounts();

        return () => {
            mounted = false;
        };
    }, [getMyAccounts]);

    useEffect(() => {
        let mounted = true;

        const loadWebhooks =
            async () => {
                try {
                    const response =
                        await getWebhooks(
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

                    setWebhooks(
                        response.data.content,
                    );

                    setTotalPages(
                        response.data.totalPages,
                    );

                    setError(null);
                } catch {
                    if (mounted) {
                        setError(
                            tErrors(
                                "fallback",
                            ),
                        );
                    }
                } finally {
                    if (mounted) {
                        setIsLoading(
                            false,
                        );
                    }
                }
            };

        void loadWebhooks();

        return () => {
            mounted = false;
        };
    }, [
        accountId,
        page,
        size,
        getWebhooks,
        getErrorMessage,
        tErrors,
        filters,
    ]);

    const handleFilterChange = (
        nextFilters: WebhookFilters,
    ) => {
        const params =
            new URLSearchParams();

        if (nextFilters.accountId) {
            params.set(
                "accountId",
                nextFilters.accountId,
            );
        }

        params.set(
            "page",
            String(
                nextFilters.page ?? 0,
            ),
        );

        params.set(
            "size",
            String(
                nextFilters.size
                ?? PAGE_SIZE,
            ),
        );

        const nextQuery =
            params.toString();

        const currentQuery =
            searchParams.toString();

        if (
            nextQuery === currentQuery
        ) {
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

    const handleRemoved = () => {
        setIsLoading(true);
        setError(null);

        void getWebhooks(filters)
            .then((response) => {
                if (!response.success) {
                    setError(
                        getErrorMessage(
                            response.code,
                        ),
                    );

                    return;
                }

                setWebhooks(
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

    const handleRegistered = () => {
        setIsRegisterModalOpen(false);

        setIsLoading(true);
        setError(null);

        void getWebhooks(filters)
            .then((response) => {
                if (!response.success) {
                    setError(
                        getErrorMessage(
                            response.code,
                        ),
                    );

                    return;
                }

                setWebhooks(
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
        <>
            <section className="
                mx-auto
                w-full
                space-y-6
            ">
                <div className="
                    flex
                    items-start
                    justify-between
                    gap-4
                ">
                    <div>
                        <h1 className="
                            text-2xl
                            font-semibold
                            text-primary
                        ">
                            {accountId
                                ? t(
                                    "list.accountTitle",
                                )
                                : t(
                                    "list.title",
                                )}
                        </h1>

                        <p className="
                            mt-1
                            text-sm
                            text-muted
                        ">
                            {accountId
                                ? t(
                                    "list.accountDescription",
                                )
                                : t(
                                    "list.description",
                                )}
                        </p>
                    </div>

                    <Button
                        type="button"
                        onClick={() =>
                            setIsRegisterModalOpen(
                                true,
                            )
                        }
                    >
                        {t(
                            "register.submit",
                        )}
                    </Button>
                </div>

                {accountId && (
                    <>
                        {isAccountsLoading && (
                            <AccountSummaryCardSkeleton />
                        )}

                        {!isAccountsLoading &&
                            account && (
                                <AccountSummaryCard
                                    account={
                                        account
                                    }
                                />
                            )}
                    </>
                )}

                <WebhookListFilters
                    accounts={accounts}
                    isAccountsLoading={
                        isAccountsLoading
                    }
                    filters={filters}
                    onChange={
                        handleFilterChange
                    }
                />

                {isLoading ? (
                    <WebhookListSkeleton />
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
                ) : webhooks.length === 0 ? (
                    <WebhookListEmpty />
                ) : (
                    <>
                        <WebhookListTable
                            webhooks={
                                webhooks
                            }
                            accounts={
                                accounts
                            }
                            onRemoved={
                                handleRemoved
                            }
                        />

                        {totalPages > 1 && (
                            <WebhookListPagination
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

            <RegisterWebhookModal
                open={
                    isRegisterModalOpen
                }
                accounts={accounts}
                onClose={() =>
                    setIsRegisterModalOpen(
                        false,
                    )
                }
                onSuccess={
                    handleRegistered
                }
            />
        </>
    );
}