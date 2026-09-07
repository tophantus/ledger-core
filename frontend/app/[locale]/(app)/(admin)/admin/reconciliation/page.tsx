"use client";

import {
    useEffect,
    useState,
} from "react";
import {useTranslations} from "next-intl";
import {useSearchParams} from "next/navigation";

import {ReconciliationSummary} from "@/features/admin/reconciliation/components/reconciliation-summary";
import {ReconciliationExceptionFilters} from "@/features/admin/reconciliation/components/reconciliation-exception-filters";
import {ReconciliationExceptionTable} from "@/features/admin/reconciliation/components/reconciliation-exception-table";
import {ReconciliationExceptionPagination} from "@/features/admin/reconciliation/components/reconciliation-exception-pagination";
import {useAdminReconciliation} from "@/features/admin/reconciliation/hooks/use-admin-reconciliation";
import type {
    ReconciliationErrorCode,
    ReconciliationException,
    ReconciliationTargetType,
    ReconciliationSummary as ReconciliationSummaryData,
} from "@/features/admin/reconciliation/types/admin-reconciliation";
import {useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

const PAGE_SIZE = 20;

export default function AdminReconciliationPage() {
    const t = useTranslations(
        "admin.reconciliation",
    );

    const router = useRouter();
    const searchParams = useSearchParams();

    const {
        getSummary,
        getExceptions,
    } = useAdminReconciliation();

    const businessDateParam =
        searchParams.get("businessDate");

    const [summary, setSummary] =
        useState<ReconciliationSummaryData | null>(
            null,
        );

    const [exceptions, setExceptions] =
        useState<ReconciliationException[]>([]);

    const [targetType, setTargetType] =
        useState<ReconciliationTargetType | "">("");

    const [errorCode, setErrorCode] =
        useState<ReconciliationErrorCode | "">("");

    const [page, setPage] = useState(0);

    const [totalPages, setTotalPages] =
        useState(0);

    const [summaryLoading, setSummaryLoading] =
        useState(true);

    const [exceptionsLoading, setExceptionsLoading] =
        useState(false);

    const [summaryError, setSummaryError] =
        useState(false);

    const [exceptionsError, setExceptionsError] =
        useState(false);

    /*
     * Load summary.
     *
     * When businessDateParam is absent,
     * backend resolves the latest business date.
     */
    useEffect(() => {
        let cancelled = false;

        const load = async () => {
            try {
                const response =
                    await getSummary(
                        businessDateParam ||
                        undefined,
                    );

                if (cancelled) {
                    return;
                }

                if (!response.success) {
                    setSummaryError(true);
                    return;
                }

                setSummaryError(false);
                setSummary(response.data);
            } catch {
                if (!cancelled) {
                    setSummaryError(true);
                }
            } finally {
                if (!cancelled) {
                    setSummaryLoading(false);
                }
            }
        };

        void load();

        return () => {
            cancelled = true;
        };
    }, [
        businessDateParam,
        getSummary,
    ]);

    /*
     * Load exceptions using the business date
     * returned by the summary.
     */
    useEffect(() => {
        if (!summary?.businessDate) {
            return;
        }

        let cancelled = false;

        const load = async () => {
            try {
                const response =
                    await getExceptions({
                        businessDate:
                        summary.businessDate,
                        targetType:
                            targetType ||
                            undefined,
                        errorCode:
                            errorCode ||
                            undefined,
                        page,
                        size: PAGE_SIZE,
                    });

                if (cancelled) {
                    return;
                }

                if (!response.success) {
                    setExceptionsError(true);
                    return;
                }

                setExceptionsError(false);
                setExceptions(
                    response.data.content,
                );
                setTotalPages(
                    response.data.totalPages,
                );
            } catch {
                if (!cancelled) {
                    setExceptionsError(true);
                }
            } finally {
                if (!cancelled) {
                    setExceptionsLoading(false);
                }
            }
        };

        void load();

        return () => {
            cancelled = true;
        };
    }, [
        summary?.businessDate,
        targetType,
        errorCode,
        page,
        getExceptions,
    ]);

    const handleDateChange = (
        businessDate: string,
    ) => {
        setPage(0);
        setTargetType("");
        setErrorCode("");

        if (!businessDate) {
            router.push(
                ROUTES.ADMIN.RECONCILIATION,
            );
            return;
        }

        router.push(
            `${ROUTES.ADMIN.RECONCILIATION}?businessDate=${businessDate}`,
        );
    };

    const handleTargetTypeChange = (
        value: ReconciliationTargetType | "",
    ) => {
        setTargetType(value);
        setPage(0);
    };

    const handleErrorCodeChange = (
        value: ReconciliationErrorCode | "",
    ) => {
        setErrorCode(value);
        setPage(0);
    };

    const handleClearFilters = () => {
        setTargetType("");
        setErrorCode("");
        setPage(0);
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

            {/* Business Date */}
            <section className="rounded-lg border border-border bg-surface p-6">
                <div className="flex flex-col gap-2">
                    <label
                        htmlFor="business-date"
                        className="text-sm font-medium text-primary"
                    >
                        {t("businessDate")}
                    </label>

                    <input
                        id="business-date"
                        type="date"
                        value={
                            summary?.businessDate ??
                            businessDateParam ??
                            ""
                        }
                        onChange={(event) =>
                            handleDateChange(
                                event.target.value,
                            )
                        }
                        className="
                            h-10 w-full max-w-xs
                            rounded-md
                            border border-border
                            bg-background
                            px-3
                            text-sm text-primary
                            outline-none
                            focus:ring-2
                            focus:ring-primary/20
                        "
                    />
                </div>
            </section>

            {/* Summary */}
            <ReconciliationSummary
                summary={summary}
                loading={summaryLoading}
                error={summaryError}
            />

            {/* Exceptions */}
            <section className="space-y-4">
                <div>
                    <h2 className="text-lg font-semibold text-primary">
                        {t(
                            "exceptions.title",
                        )}
                    </h2>

                    <p className="mt-1 text-sm text-muted">
                        {t(
                            "exceptions.description",
                        )}
                    </p>
                </div>

                <div className="overflow-hidden rounded-lg border border-border bg-surface">
                    <ReconciliationExceptionFilters
                        targetType={targetType}
                        errorCode={errorCode}
                        onTargetTypeChange={
                            handleTargetTypeChange
                        }
                        onErrorCodeChange={
                            handleErrorCodeChange
                        }
                        onClear={
                            handleClearFilters
                        }
                    />

                    <ReconciliationExceptionTable
                        exceptions={exceptions}
                        loading={
                            exceptionsLoading
                        }
                        error={exceptionsError}
                    />

                    {!exceptionsLoading &&
                        !exceptionsError &&
                        exceptions.length > 0 && (
                            <ReconciliationExceptionPagination
                                page={page}
                                totalPages={
                                    totalPages
                                }
                                onPrevious={() =>
                                    setPage(
                                        (value) =>
                                            value - 1,
                                    )
                                }
                                onNext={() =>
                                    setPage(
                                        (value) =>
                                            value + 1,
                                    )
                                }
                            />
                        )}
                </div>
            </section>
        </div>
    );
}