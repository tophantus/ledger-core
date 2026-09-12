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
import {useAdminInterestRuns} from "@/features/admin/interest/hooks/use-admin-interest-runs";
import type {PageResponse} from "@/lib/api/types";
import {ROUTES} from "@/lib/constants/routes";

import {AdminInterestRunFilters} from "./admin-interest-run-filters";
import {AdminInterestRunList} from "./admin-interest-run-list";

import type {
    InterestRun,
    InterestRunFilters,
    InterestRunStatus,
    InterestRunType,
} from "../types/admin-interest";

const PAGE_SIZE = 20;

const INTEREST_RUN_TYPES: InterestRunType[] = [
    "ACCRUAL",
    "POSTING",
];

const INTEREST_RUN_STATUSES: InterestRunStatus[] = [
    "PENDING",
    "RUNNING",
    "COMPLETED",
];

function getRunType(
    value: string | null,
): InterestRunType | undefined {
    if (!value) {
        return undefined;
    }

    return INTEREST_RUN_TYPES.includes(
        value as InterestRunType,
    )
        ? (value as InterestRunType)
        : undefined;
}

function getRunStatus(
    value: string | null,
): InterestRunStatus | undefined {
    if (!value) {
        return undefined;
    }

    return INTEREST_RUN_STATUSES.includes(
        value as InterestRunStatus,
    )
        ? (value as InterestRunStatus)
        : undefined;
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

export default function AdminInterestRunListPage() {
    const t = useTranslations(
        "admin.interest.runs",
    );

    const router = useRouter();
    const searchParams = useSearchParams();

    const {getRuns} =
        useAdminInterestRuns();

    const businessDate =
        searchParams.get("businessDate")
        ?? undefined;

    const fromDate =
        searchParams.get("fromDate")
        ?? undefined;

    const toDate =
        searchParams.get("toDate")
        ?? undefined;

    const runType =
        getRunType(
            searchParams.get("runType"),
        );

    const status =
        getRunStatus(
            searchParams.get("status"),
        );

    const page = getPage(
        searchParams.get("page"),
    );

    const size = getSize(
        searchParams.get("size"),
    );

    const filters =
        useMemo<InterestRunFilters>(
            () => ({
                businessDate,
                fromDate,
                toDate,
                runType,
                status,
                page,
                size,
            }),
            [
                businessDate,
                fromDate,
                toDate,
                runType,
                status,
                page,
                size,
            ],
        );

    const [result, setResult] =
        useState<
            PageResponse<InterestRun> | null
        >(null);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState<string | null>(null);

    useEffect(() => {
        let mounted = true;

        const loadRuns = async () => {
            try {
                const response =
                    await getRuns(filters);

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

        void loadRuns();

        return () => {
            mounted = false;
        };
    }, [
        businessDate,
        fromDate,
        toDate,
        runType,
        status,
        page,
        size,
        getRuns,
        t,
        filters,
    ]);

    const handleFilterChange = (
        nextFilters: InterestRunFilters,
    ) => {
        const params =
            new URLSearchParams();

        if (nextFilters.businessDate) {
            params.set(
                "businessDate",
                nextFilters.businessDate,
            );
        }

        if (nextFilters.fromDate) {
            params.set(
                "fromDate",
                nextFilters.fromDate,
            );
        }

        if (nextFilters.toDate) {
            params.set(
                "toDate",
                nextFilters.toDate,
            );
        }

        if (nextFilters.runType) {
            params.set(
                "runType",
                nextFilters.runType,
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
            `${ROUTES.ADMIN.INTERESTS.RUNS}?${nextQuery}`,
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
            <div className="space-y-2">
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
            </div>

            <AdminInterestRunFilters
                filters={filters}
                onChange={handleFilterChange}
            />

            <AdminInterestRunList
                runs={result?.content ?? []}
                loading={loading}
            />

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
                                    loading ||
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
                                    loading ||
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