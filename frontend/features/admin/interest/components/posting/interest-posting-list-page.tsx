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
import {useAdminInterestPostings} from "@/features/admin/interest/hooks/use-admin-interest-postings";
import type {PageResponse} from "@/lib/api/types";
import {ROUTES} from "@/lib/constants/routes";

import {InterestPostingListFilters} from "./interest-posting-list-filters";
import {InterestPostingListTable} from "./interest-posting-list-table";
import {InterestPostingListSkeleton} from "./interest-posting-list-skeleton";
import {InterestPostingListEmpty} from "./interest-posting-list-empty";

import type {
    InterestPosting,
    InterestPostingFilters,
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

export default function InterestPostingListPage() {
    const t = useTranslations(
        "admin.interest.postings",
    );

    const router = useRouter();
    const searchParams = useSearchParams();

    const {
        getPostings,
    } = useAdminInterestPostings();

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
        useMemo<InterestPostingFilters>(
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
        PageResponse<InterestPosting> | null
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
     * => set businessDate to yesterday
     *    before fetching data.
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
            `${ROUTES.ADMIN.INTERESTS.POSTINGS}?${params.toString()}`,
        );
    }, [
        shouldSetDefaultBusinessDate,
        searchParams,
        router,
    ]);

    /*
     * Fetch only after the URL has been
     * initialized with the default date.
     */
    useEffect(() => {
        if (shouldSetDefaultBusinessDate) {
            return;
        }

        let mounted = true;

        const loadPostings = async () => {
            try {
                const response =
                    await getPostings(
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

        void loadPostings();

        return () => {
            mounted = false;
        };
    }, [shouldSetDefaultBusinessDate, runId, businessDate, accountId, page, size, getPostings, t, filters]);

    const handleFilterChange = (
        nextFilters: InterestPostingFilters,
    ) => {
        const params =
            new URLSearchParams();

        if (nextFilters.runId) {
            params.set(
                "runId",
                nextFilters.runId,
            );
        }

        if (nextFilters.accountId) {
            params.set(
                "accountId",
                nextFilters.accountId,
            );
        }

        if (nextFilters.businessDate) {
            params.set(
                "businessDate",
                nextFilters.businessDate,
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
            `${ROUTES.ADMIN.INTERESTS.POSTINGS}?${nextQuery}`,
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

            <InterestPostingListFilters
                filters={filters}
                onChange={
                    handleFilterChange
                }
            />

            {loading ? (
                <InterestPostingListSkeleton />
            ) : result?.content.length ? (
                <InterestPostingListTable
                    postings={
                        result.content
                    }
                />
            ) : (
                <InterestPostingListEmpty />
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