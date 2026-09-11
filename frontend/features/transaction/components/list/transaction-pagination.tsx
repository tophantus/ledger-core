"use client";

import {ChevronLeft, ChevronRight} from "lucide-react";
import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";

interface TransactionPaginationProps {
    page: number;
    totalPages: number;
    onPageChange: (page: number) => void;
}

export function TransactionPagination({
                                          page,
                                          totalPages,
                                          onPageChange,
                                      }: TransactionPaginationProps) {
    const t = useTranslations("transaction");

    if (totalPages <= 1) {
        return null;
    }

    const isFirstPage = page === 0;
    const isLastPage = page >= totalPages - 1;

    return (
        <div className="flex items-center justify-between">
            <p className="text-sm text-text-muted">
                {t("pagination.page", {
                    current: page + 1,
                    total: totalPages,
                })}
            </p>

            <div className="flex items-center gap-2">
                <Button
                    type="button"
                    variant="outline"
                    disabled={isFirstPage}
                    onClick={() =>
                        onPageChange(page - 1)
                    }
                >
                    <ChevronLeft className="h-4 w-4" />
                    <span className="hidden sm:inline">
                        {t("pagination.previous")}
                    </span>
                </Button>

                <Button
                    type="button"
                    variant="outline"
                    disabled={isLastPage}
                    onClick={() =>
                        onPageChange(page + 1)
                    }
                >
                    <span className="hidden sm:inline">
                        {t("pagination.next")}
                    </span>
                    <ChevronRight className="h-4 w-4" />
                </Button>
            </div>
        </div>
    );
}