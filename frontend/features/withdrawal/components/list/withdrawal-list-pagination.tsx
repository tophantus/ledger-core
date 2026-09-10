"use client";

import {
    ChevronLeft,
    ChevronRight,
} from "lucide-react";
import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";

interface WithdrawalListPaginationProps {
    page: number;
    totalPages: number;
    onPageChange: (
        page: number,
    ) => void;
}

export function WithdrawalListPagination({
                                             page,
                                             totalPages,
                                             onPageChange,
                                         }: WithdrawalListPaginationProps) {
    const t =
        useTranslations("withdrawal");

    if (totalPages <= 1) {
        return null;
    }

    const isFirstPage = page === 0;
    const isLastPage =
        page >= totalPages - 1;

    return (
        <div className="
            flex
            items-center
            justify-between
            gap-4
        ">
            <p className="
                text-sm
                text-muted
            ">
                {t("list.page", {
                    current: page + 1,
                    total: totalPages,
                })}
            </p>

            <div className="
                flex
                items-center
                gap-2
            ">
                <Button
                    type="button"
                    variant="outline"
                    disabled={
                        isFirstPage
                    }
                    onClick={() =>
                        onPageChange(
                            page - 1,
                        )
                    }
                >
                    <ChevronLeft className="h-4 w-4" />
                    {t("list.previous")}
                </Button>

                <Button
                    type="button"
                    variant="outline"
                    disabled={
                        isLastPage
                    }
                    onClick={() =>
                        onPageChange(
                            page + 1,
                        )
                    }
                >
                    {t("list.next")}
                    <ChevronRight className="h-4 w-4" />
                </Button>
            </div>
        </div>
    );
}