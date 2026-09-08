"use client";

import {useEffect, useState} from "react";
import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";
import {useAdminAccounts} from "@/features/admin/account/hooks/use-admin-accounts";
import {useDebouncedValue} from "@/lib/hooks/use-debounced-value";

import {AdminAccountFilters} from "@/features/admin/account/components/admin-account-filters";
import {AdminAccountList} from "@/features/admin/account/components/admin-account-list";

import type {
    AdminAccount,
    AdminAccountFilters as AccountFilters,
} from "@/features/admin/account/types/admin-account";

const PAGE_SIZE = 20;

export default function AdminAccountsPageContent() {
    const t = useTranslations("admin.account");

    const {getAdminAccounts} =
        useAdminAccounts();

    const [filters, setFilters] =
        useState<AccountFilters>({
            page: 0,
            size: PAGE_SIZE,
        });

    const debouncedAccountNo =
        useDebouncedValue(
            filters.accountNo,
            400,
        );

    const [accounts, setAccounts] =
        useState<AdminAccount[]>([]);

    const [totalElements, setTotalElements] =
        useState(0);

    const [totalPages, setTotalPages] =
        useState(0);

    const [loading, setLoading] =
        useState(false);

    const [error, setError] =
        useState<string | null>(null);

    useEffect(() => {
        const loadAccounts = async () => {
            setLoading(true);
            setError(null);

            try {
                const response =
                    await getAdminAccounts({
                        ...filters,
                        accountNo:
                            debouncedAccountNo ||
                            undefined,
                    });

                const page = response.data;

                setAccounts(page.content);
                setTotalElements(
                    page.totalElements,
                );
                setTotalPages(page.totalPages);
            } catch {
                setError(
                    t("loadError"),
                );
            } finally {
                setLoading(false);
            }
        };

        void loadAccounts();
    }, [filters.page, filters.size, filters.status, filters.currency, filters.userId, debouncedAccountNo, getAdminAccounts, t, filters]);

    const handleFiltersChange = (
        nextFilters: AccountFilters,
    ) => {
        setFilters(nextFilters);
    };

    const handleClear = () => {
        setFilters({
            page: 0,
            size: PAGE_SIZE,
        });
    };

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-semibold text-primary">
                    {t("title")}
                </h1>

                <p className="mt-1 text-sm text-muted">
                    {t("description")}
                </p>
            </div>

            <div className="space-y-4">
                <AdminAccountFilters
                    filters={filters}
                    onChange={
                        handleFiltersChange
                    }
                />

                <div className="flex justify-end">
                    <Button
                        variant="outline"
                        onClick={handleClear}
                    >
                        {t("filters.clear")}
                    </Button>
                </div>
            </div>

            {error && (
                <div className="rounded-md border border-border bg-surface p-4 text-sm text-danger">
                    {error}
                </div>
            )}

            <AdminAccountList
                accounts={accounts}
                loading={loading}
            />

            <div className="flex items-center justify-between">
                <span className="text-sm text-muted">
                    {t("pagination.total", {
                        count: totalElements,
                    })}
                </span>

                <div className="flex gap-2">
                    <Button
                        variant="outline"
                        disabled={
                            loading ||
                            filters.page === 0
                        }
                        onClick={() =>
                            setFilters(
                                (current) => ({
                                    ...current,
                                    page:
                                        (current.page ??
                                            0) - 1,
                                }),
                            )
                        }
                    >
                        {t(
                            "pagination.previous",
                        )}
                    </Button>

                    <span className="flex items-center px-2 text-sm text-muted">
                        {t(
                            "pagination.page",
                            {
                                current:
                                    (filters.page ??
                                        0) + 1,
                                total:
                                totalPages,
                            },
                        )}
                    </span>

                    <Button
                        variant="outline"
                        disabled={
                            loading ||
                            totalPages === 0 ||
                            (filters.page ??
                                0) >=
                            totalPages - 1
                        }
                        onClick={() =>
                            setFilters(
                                (current) => ({
                                    ...current,
                                    page:
                                        (current.page ??
                                            0) + 1,
                                }),
                            )
                        }
                    >
                        {t("pagination.next")}
                    </Button>
                </div>
            </div>
        </div>
    );
}