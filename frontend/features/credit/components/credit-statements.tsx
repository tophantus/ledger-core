"use client";

import {
    useCallback,
    useEffect,
    useState,
} from "react";
import {
    ChevronLeft,
    ChevronRight,
    FileText,
} from "lucide-react";
import {useTranslations} from "next-intl";

import {useGetCreditStatements} from "../hooks/use-get-credit-statements";
import type {GetUserCreditFacilityResult} from "../types/credit-facility";
import type {CreditStatement} from "../types/credit-statement";

import {CreditStatementCard} from "./credit-statement-card";
import {CreditStatementsEmpty} from "./credit-statements-empty";
import {CreditStatementsSkeleton} from "./credit-statements-skeleton";

interface CreditStatementsProps {
    facility: GetUserCreditFacilityResult;
}

const PAGE_SIZE = 5;

export function CreditStatements({
                                     facility,
                                 }: CreditStatementsProps) {
    const t = useTranslations("credit");

    const {getStatements} =
        useGetCreditStatements();

    const [statements, setStatements] =
        useState<CreditStatement[]>([]);

    const [page, setPage] = useState(0);

    const [totalPages, setTotalPages] =
        useState(0);

    const [totalElements, setTotalElements] =
        useState(0);

    const [isLoading, setIsLoading] =
        useState(true);

    const [hasError, setHasError] =
        useState(false);

    const loadStatements = useCallback(
        async (pageNumber: number) => {
            setIsLoading(true);
            setHasError(false);

            try {
                const response =
                    await getStatements(
                        facility.id,
                        pageNumber,
                        PAGE_SIZE,
                    );

                if (!response.success) {
                    setHasError(true);
                    return;
                }

                setStatements(
                    response.data.content,
                );

                setPage(
                    response.data.page,
                );

                setTotalPages(
                    response.data.totalPages,
                );

                setTotalElements(
                    response.data.totalElements,
                );
            } catch {
                setHasError(true);
            } finally {
                setIsLoading(false);
            }
        },
        [facility.id, getStatements],
    );

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        void loadStatements(0);
    }, [loadStatements]);

    const handlePrevious = () => {
        if (page <= 0 || isLoading) {
            return;
        }

        void loadStatements(page - 1);
    };

    const handleNext = () => {
        if (
            page >= totalPages - 1 ||
            isLoading
        ) {
            return;
        }

        void loadStatements(page + 1);
    };

    return (
        <section className="mt-6">
            <div className="mb-4 flex items-center gap-3">
                <div
                    className="
                        flex h-9 w-9 items-center justify-center
                        rounded-lg bg-primary/10
                    "
                >
                    <FileText
                        className="h-5 w-5 text-primary"
                    />
                </div>

                <div>
                    <h2 className="text-lg font-semibold text-text-primary">
                        {t("statements.title")}
                    </h2>

                    <p className="text-sm text-text-muted">
                        {t("statements.description")}
                    </p>
                </div>
            </div>

            {isLoading ? (
                <CreditStatementsSkeleton />
            ) : hasError ? (
                <div
                    className="
                        rounded-xl border border-border
                        bg-surface-subtle p-5
                    "
                >
                    <p className="text-sm text-danger">
                        {t("statements.loadFailed")}
                    </p>
                </div>
            ) : statements.length === 0 ? (
                <CreditStatementsEmpty />
            ) : (
                <>
                    <div className="space-y-3">
                        {statements.map(
                            (statement) => (
                                <CreditStatementCard
                                    key={statement.id}
                                    statement={statement}
                                    currency={facility.currency}
                                />
                            ),
                        )}
                    </div>

                    {totalPages > 1 && (
                        <div
                            className="
                                mt-4 flex flex-col gap-3
                                border-t border-border pt-4
                                sm:flex-row sm:items-center
                                sm:justify-between
                            "
                        >
                            <p className="text-sm text-text-muted">
                                {t(
                                    "statements.pagination.summary",
                                    {
                                        page:
                                            page + 1,
                                        totalPages,
                                        totalElements,
                                    },
                                )}
                            </p>

                            <div className="flex items-center gap-2">
                                <button
                                    type="button"
                                    onClick={
                                        handlePrevious
                                    }
                                    disabled={
                                        page === 0 ||
                                        isLoading
                                    }
                                    className="
                                        inline-flex items-center
                                        gap-1 rounded-lg
                                        border border-border
                                        bg-surface px-3 py-2
                                        text-sm font-medium
                                        text-text-primary
                                        transition
                                        hover:bg-surface-subtle
                                        disabled:cursor-not-allowed
                                        disabled:opacity-50
                                    "
                                >
                                    <ChevronLeft className="h-4 w-4" />

                                    {t(
                                        "statements.pagination.previous",
                                    )}
                                </button>

                                <span
                                    className="
                                        min-w-20 text-center
                                        text-sm text-text-muted
                                    "
                                >
                                    {page + 1} /{" "}
                                    {totalPages}
                                </span>

                                <button
                                    type="button"
                                    onClick={
                                        handleNext
                                    }
                                    disabled={
                                        page >=
                                        totalPages -
                                        1 ||
                                        isLoading
                                    }
                                    className="
                                        inline-flex items-center
                                        gap-1 rounded-lg
                                        border border-border
                                        bg-surface px-3 py-2
                                        text-sm font-medium
                                        text-text-primary
                                        transition
                                        hover:bg-surface-subtle
                                        disabled:cursor-not-allowed
                                        disabled:opacity-50
                                    "
                                >
                                    {t(
                                        "statements.pagination.next",
                                    )}

                                    <ChevronRight className="h-4 w-4" />
                                </button>
                            </div>
                        </div>
                    )}
                </>
            )}
        </section>
    );
}