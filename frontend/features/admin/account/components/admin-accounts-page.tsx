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
import {useAdminAccounts} from "@/features/admin/account/hooks/use-admin-accounts";
import {useDebouncedValue} from "@/lib/hooks/use-debounced-value";
import {
    getCurrency,
} from "@/lib/constants/currency";
import {ROUTES} from "@/lib/constants/routes";

import {AdminAccountFilters} from "./admin-account-filters";
import {AdminAccountList} from "./admin-account-list";

import type {
    AdminAccount,
    AdminAccountFilters as AccountFilters,
    AccountStatus,
} from "../types/admin-account";

const PAGE_SIZE = 20;

function getAccountStatus(
    value: string | null,
): AccountStatus | undefined {
    if (
        value === "ACTIVE" ||
        value === "BLOCKED" ||
        value === "CLOSED"
    ) {
        return value;
    }

    return undefined;
}

function getPage(
    value: string | null,
): number {
    const page = Number(value);

    return Number.isInteger(page) &&
    page >= 0
        ? page
        : 0;
}

function getSize(
    value: string | null,
): number {
    const size = Number(value);

    return Number.isInteger(size) &&
    size > 0
        ? size
        : PAGE_SIZE;
}

export default function AdminAccountsPageContent() {
    const t = useTranslations(
        "admin.account",
    );

    const router = useRouter();
    const searchParams = useSearchParams();

    const {getAdminAccounts} =
        useAdminAccounts();

    const accountNo =
        searchParams.get("accountNo")
        ?? undefined;

    const status = getAccountStatus(
        searchParams.get("status"),
    );

    const currencyParam =
        searchParams.get("currency");

    const currency =
        currencyParam
            ? getCurrency(currencyParam)
            : undefined;

    const userId =
        searchParams.get("userId")
        ?? undefined;

    const page = getPage(
        searchParams.get("page"),
    );

    const size = getSize(
        searchParams.get("size"),
    );

    const filters =
        useMemo<AccountFilters>(
            () => ({
                accountNo,
                status,
                currency,
                userId,
                page,
                size,
            }),
            [
                accountNo,
                status,
                currency,
                userId,
                page,
                size,
            ],
        );

    const debouncedAccountNo =
        useDebouncedValue(
            accountNo,
            400,
        );

    const [accounts, setAccounts] =
        useState<AdminAccount[]>([]);

    const [totalElements, setTotalElements] =
        useState(0);

    const [totalPages, setTotalPages] =
        useState(0);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState<string | null>(null);

    const getErrorMessage =
        useCallback(
            (message?: string) =>
                message ||
                t("loadError"),
            [t],
        );

    useEffect(() => {
        let mounted = true;

        const loadAccounts = async () => {
            try {
                const response =
                    await getAdminAccounts({
                        ...filters,
                        accountNo:
                            debouncedAccountNo ||
                            undefined,
                    });

                if (!mounted) {
                    return;
                }

                if (!response.success) {
                    setError(
                        getErrorMessage(
                            response.message,
                        ),
                    );

                    return;
                }

                const result =
                    response.data;

                setAccounts(
                    result.content,
                );

                setTotalElements(
                    result.totalElements,
                );

                setTotalPages(
                    result.totalPages,
                );

                setError(null);
            } catch {
                if (mounted) {
                    setError(
                        t("loadError"),
                    );
                }
            } finally {
                if (mounted) {
                    setLoading(false);
                }
            }
        };

        void loadAccounts();

        return () => {
            mounted = false;
        };
    }, [
        filters,
        debouncedAccountNo,
        getAdminAccounts,
        getErrorMessage,
        t,
    ]);

    const handleFilterChange = (
        nextFilters: AccountFilters,
    ) => {
        const params =
            new URLSearchParams();

        if (nextFilters.accountNo) {
            params.set(
                "accountNo",
                nextFilters.accountNo,
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

        if (nextFilters.userId) {
            params.set(
                "userId",
                nextFilters.userId,
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
                nextFilters.size ??
                PAGE_SIZE,
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

        setLoading(true);
        setError(null);

        router.replace(
            `${ROUTES.ADMIN.ACCOUNTS}?${nextQuery}`,
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

    const handleClear = () => {
        handleFilterChange({
            page: 0,
            size: PAGE_SIZE,
        });
    };

    const currentPage =
        filters.page ?? 0;

    return (
        <div className="space-y-6">
            <div>
                <h1 className="
                    text-2xl
                    font-semibold
                    text-primary
                ">
                    {t("title")}
                </h1>

                <p className="
                    mt-1
                    text-sm
                    text-muted
                ">
                    {t("description")}
                </p>
            </div>

            <div className="space-y-4">
                <AdminAccountFilters
                    filters={filters}
                    onChange={
                        handleFilterChange
                    }
                />

                <div className="
                    flex
                    justify-end
                ">
                    <Button
                        variant="outline"
                        onClick={
                            handleClear
                        }
                    >
                        {t(
                            "filters.clear",
                        )}
                    </Button>
                </div>
            </div>

            {error && (
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
                        {error}
                    </p>
                </div>
            )}

            <AdminAccountList
                accounts={accounts}
                loading={loading}
            />

            <div className="
                flex
                items-center
                justify-between
            ">
                <span className="
                    text-sm
                    text-muted
                ">
                    {t(
                        "pagination.total",
                        {
                            count:
                            totalElements,
                        },
                    )}
                </span>

                <div className="
                    flex
                    gap-2
                ">
                    <Button
                        variant="outline"
                        disabled={
                            loading ||
                            currentPage ===
                            0
                        }
                        onClick={() =>
                            handlePageChange(
                                currentPage -
                                1,
                            )
                        }
                    >
                        {t(
                            "pagination.previous",
                        )}
                    </Button>

                    <span className="
                        flex
                        items-center
                        px-2
                        text-sm
                        text-muted
                    ">
                        {t(
                            "pagination.page",
                            {
                                current:
                                    currentPage +
                                    1,
                                total:
                                totalPages,
                            },
                        )}
                    </span>

                    <Button
                        variant="outline"
                        disabled={
                            loading ||
                            totalPages ===
                            0 ||
                            currentPage >=
                            totalPages -
                            1
                        }
                        onClick={() =>
                            handlePageChange(
                                currentPage +
                                1,
                            )
                        }
                    >
                        {t(
                            "pagination.next",
                        )}
                    </Button>
                </div>
            </div>
        </div>
    );
}