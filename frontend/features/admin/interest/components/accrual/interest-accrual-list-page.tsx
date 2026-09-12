"use client";

import {
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
import {useAdminInterestAccruals} from "@/features/admin/interest/hooks/use-admin-interest-accruals";
import type {PageResponse} from "@/lib/api/types";
import {ROUTES} from "@/lib/constants/routes";

import {InterestAccrualListFilters} from "./interest-accrual-list-filters";
import {InterestAccrualListTable} from "./interest-accrual-list-table";
import {InterestAccrualListSkeleton} from "./interest-accrual-list-skeleton";
import {InterestAccrualListEmpty} from "./interest-accrual-list-empty";

import type {
    InterestAccrual,
    InterestAccrualFilters,
} from "../../types/admin-interest";

const PAGE_SIZE = 20;

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

function getDefaultBusinessDate(): string {
    const date = new Date();

    date.setDate(
        date.getDate() - 1,
    );

    return date.toISOString().split("T")[0];
}

export default function InterestAccrualListPage() {
    const t = useTranslations(
        "admin.interest.accruals",
    );

    const router = useRouter();
    const searchParams = useSearchParams();

    const {
        getAccruals,
    } = useAdminInterestAccruals();

    const runId =
        searchParams.get("runId")
        ?? undefined;

    const accountId =
        searchParams.get("accountId")
        ?? undefined;

    const businessDate =
        searchParams.get("businessDate")
        ?? undefined;

    const page = getPage(
        searchParams.get("page"),
    );

    const size = getSize(
        searchParams.get("size"),
    );

    const shouldSetDefaultBusinessDate =
        !runId &&
        !accountId &&
        !businessDate;

    const filters =
        useMemo<InterestAccrualFilters>(
            () => ({
                runId,
                businessDate,
                accountId,
                page,
                size,
            }),
            [
                runId,
                businessDate,
                accountId,
                page,
                size,
            ],
        );

    const [
        result,
        setResult,
    ] = useState<
        PageResponse<InterestAccrual> | null
    >(null);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState<string | null>(null);

    /*
     * First visit:
     *
     * No runId
     * No accountId
     * No businessDate
     *
     * => default to yesterday.
     */
    useEffect(() => {
        if (!shouldSetDefaultBusinessDate) {
            return;
        }

        const params =
            new URLSearchParams(
                searchParams.toString(),
            );

        params.set(
            "businessDate",
            getDefaultBusinessDate(),
        );

        params.set(
            "page",
            "0",
        );

        params.set(
            "size",
            String(PAGE_SIZE),
        );

        router.replace(
            `${ROUTES.ADMIN.INTERESTS.ACCRUALS}?${params.toString()}`,
        );
    }, [
        shouldSetDefaultBusinessDate,
        searchParams,
        router,
    ]);

    /*
     * Only load data after the URL has
     * been normalized with the default date.
     */
    useEffect(() => {
        if (shouldSetDefaultBusinessDate) {
            return;
        }

        let mounted = true;

        const loadAccruals = async () => {
            try {
                const response =
                    await getAccruals(
                        filters,
                    );

                if (!mounted) {
                    return;
                }

                if (!response.success) {
                    setError(
                        response.message ||
                        t("loadError"),
                    );

                    return;
                }

                setResult(response.data);
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

        void loadAccruals();

        return () => {
            mounted = false;
        };
    }, [shouldSetDefaultBusinessDate, runId, businessDate, accountId, page, size, getAccruals, t, filters]);

    const handleFilterChange = (
        nextFilters: InterestAccrualFilters,
    ) => {
        const params =
            new URLSearchParams();

        if (nextFilters.runId) {
            params.set(
                "runId",
                nextFilters.runId,
            );
        }

        if (nextFilters.businessDate) {
            params.set(
                "businessDate",
                nextFilters.businessDate,
            );
        }

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
                nextFilters.size ??
                PAGE_SIZE,
            ),
        );

        const nextQuery =
            params.toString();

        const currentQuery =
            searchParams.toString();

        if (nextQuery === currentQuery) {
            return;
        }

        setLoading(true);
        setError(null);

        router.replace(
            `${ROUTES.ADMIN.INTERESTS.ACCRUALS}?${nextQuery}`,
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

    return (
        <section className="space-y-6">
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

            <InterestAccrualListFilters
                filters={filters}
                onChange={
                    handleFilterChange
                }
            />

            {loading ? (
                <InterestAccrualListSkeleton />
            ) : result?.content.length ? (
                <InterestAccrualListTable
                    accruals={
                        result.content
                    }
                />
            ) : (
                <InterestAccrualListEmpty />
            )}

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

            {!loading &&
                !error &&
                result &&
                result.totalPages > 1 && (
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
                                    result.totalElements,
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
                                    result.page ===
                                    0
                                }
                                onClick={() =>
                                    handlePageChange(
                                        result.page -
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
                                            result.page +
                                            1,
                                        total:
                                        result.totalPages,
                                    },
                                )}
                            </span>

                            <Button
                                variant="outline"
                                disabled={
                                    result.page >=
                                    result.totalPages -
                                    1
                                }
                                onClick={() =>
                                    handlePageChange(
                                        result.page +
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
                )}
        </section>
    );
}