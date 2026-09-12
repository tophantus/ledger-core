"use client";

import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";

interface InterestAccrualListPaginationProps {
    page: number;
    totalPages: number;
    onPageChange: (
        page: number,
    ) => void;
}

export function InterestAccrualListPagination({
                                                  page,
                                                  totalPages,
                                                  onPageChange,
                                              }: InterestAccrualListPaginationProps) {
    const t = useTranslations(
        "admin.interest.accruals.pagination",
    );

    if (totalPages <= 1) {
        return null;
    }

    return (
        <div className="
            flex
            items-center
            gap-2
        ">
            <Button
                type="button"
                variant="secondary"
                disabled={page === 0}
                onClick={() =>
                    onPageChange(
                        page - 1,
                    )
                }
            >
                {t("previous")}
            </Button>

            <span className="
                min-w-20
                text-center
                text-sm
                text-muted
            ">
                {t("page", {
                    current: page + 1,
                    total: totalPages,
                })}
            </span>

            <Button
                type="button"
                variant="secondary"
                disabled={
                    page >= totalPages - 1
                }
                onClick={() =>
                    onPageChange(
                        page + 1,
                    )
                }
            >
                {t("next")}
            </Button>
        </div>
    );
}