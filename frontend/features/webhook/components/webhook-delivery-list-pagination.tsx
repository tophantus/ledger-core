"use client";

import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";

interface WebhookDeliveryListPaginationProps {
    page: number;
    totalPages: number;
    onPageChange: (
        page: number,
    ) => void;
}

export function WebhookDeliveryListPagination({
                                                  page,
                                                  totalPages,
                                                  onPageChange,
                                              }: WebhookDeliveryListPaginationProps) {
    const t =
        useTranslations("webhook");

    return (
        <div className="
            flex
            items-center
            justify-between
            gap-4
        ">
            <Button
                type="button"
                variant="outline"
                disabled={page <= 0}
                onClick={() =>
                    onPageChange(
                        page - 1,
                    )
                }
            >
                {t(
                    "list.previous",
                )}
            </Button>

            <span className="
                text-sm
                text-muted
            ">
                {t(
                    "list.page",
                    {
                        current:
                            page + 1,
                        total:
                        totalPages,
                    },
                )}
            </span>

            <Button
                type="button"
                variant="outline"
                disabled={
                    page >=
                    totalPages - 1
                }
                onClick={() =>
                    onPageChange(
                        page + 1,
                    )
                }
            >
                {t(
                    "list.next",
                )}
            </Button>
        </div>
    );
}