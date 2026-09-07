"use client";

import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";

interface ReconciliationExceptionPaginationProps {
    page: number;
    totalPages: number;
    onPrevious: () => void;
    onNext: () => void;
}

export function ReconciliationExceptionPagination({
                                                      page,
                                                      totalPages,
                                                      onPrevious,
                                                      onNext,
                                                  }: ReconciliationExceptionPaginationProps) {
    const t = useTranslations(
        "admin.reconciliation",
    );

    return (
        <div className="flex items-center justify-between border-t border-border px-4 py-3">
            <span className="text-sm text-muted">
                {t(
                    "exceptions.pagination.page",
                    {
                        current: page + 1,
                        total: totalPages,
                    },
                )}
            </span>

            <div className="flex gap-2">
                <Button
                    type="button"
                    variant="outline"
                    disabled={page === 0}
                    onClick={onPrevious}
                >
                    {t(
                        "exceptions.pagination.previous",
                    )}
                </Button>

                <Button
                    type="button"
                    variant="outline"
                    disabled={
                        page >=
                        totalPages - 1
                    }
                    onClick={onNext}
                >
                    {t(
                        "exceptions.pagination.next",
                    )}
                </Button>
            </div>
        </div>
    );
}