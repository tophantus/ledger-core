"use client";

import {
    useState,
    type ChangeEvent,
} from "react";
import {
    ChevronDown,
    ChevronUp,
} from "lucide-react";
import {useTranslations} from "next-intl";

import type {
    InterestPostingFilters,
} from "../../types/admin-interest";

interface InterestPostingListFiltersProps {
    filters: InterestPostingFilters;
    onChange: (
        filters: InterestPostingFilters,
    ) => void;
}

export function InterestPostingListFilters({
                                               filters,
                                               onChange,
                                           }: InterestPostingListFiltersProps) {
    const t = useTranslations(
        "admin.interest.postings",
    );

    const [expanded, setExpanded] =
        useState(false);

    const handleRunIdChange = (
        event: ChangeEvent<HTMLInputElement>,
    ) => {
        onChange({
            ...filters,
            runId:
                event.target.value ||
                undefined,
            page: 0,
        });
    };

    const handleAccountIdChange = (
        event: ChangeEvent<HTMLInputElement>,
    ) => {
        onChange({
            ...filters,
            accountId:
                event.target.value ||
                undefined,
            page: 0,
        });
    };

    const handleBusinessDateChange = (
        event: ChangeEvent<HTMLInputElement>,
    ) => {
        onChange({
            ...filters,
            businessDate:
                event.target.value ||
                undefined,
            page: 0,
        });
    };

    const handleClear = () => {
        onChange({
            businessDate:
                getDefaultBusinessDate(),
            page: 0,
            size: filters.size ?? 20,
        });
    };

    return (
        <div className="
            overflow-hidden
            rounded-lg
            border
            border-border
            bg-surface
        ">
            <button
                type="button"
                onClick={() =>
                    setExpanded(
                        (current) =>
                            !current,
                    )
                }
                className="
                    flex
                    w-full
                    items-center
                    justify-between
                    px-4
                    py-3
                    text-left
                    transition
                    hover:bg-background-subtle
                "
            >
                <span className="
                    text-sm
                    font-medium
                    text-foreground
                ">
                    {t("filters.title")}
                </span>

                {expanded ? (
                    <ChevronUp className="
                        h-4
                        w-4
                        text-muted
                    " />
                ) : (
                    <ChevronDown className="
                        h-4
                        w-4
                        text-muted
                    " />
                )}
            </button>

            {expanded && (
                <div className="
                    border-t
                    border-border
                    p-4
                ">
                    <div className="
                        grid
                        gap-4
                        sm:grid-cols-2
                        lg:grid-cols-4
                    ">
                        <div>
                            <label
                                htmlFor="interest-posting-run-id"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "filters.runId",
                                )}
                            </label>

                            <input
                                id="interest-posting-run-id"
                                type="text"
                                value={
                                    filters.runId ??
                                    ""
                                }
                                onChange={
                                    handleRunIdChange
                                }
                                placeholder={t(
                                    "filters.runIdPlaceholder",
                                )}
                                className="
                                    w-full
                                    rounded-md
                                    border
                                    border-border
                                    bg-background
                                    px-3
                                    py-2
                                    text-sm
                                    text-foreground
                                    outline-none
                                "
                            />
                        </div>

                        <div>
                            <label
                                htmlFor="interest-posting-account-id"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "filters.accountId",
                                )}
                            </label>

                            <input
                                id="interest-posting-account-id"
                                type="text"
                                value={
                                    filters.accountId ??
                                    ""
                                }
                                onChange={
                                    handleAccountIdChange
                                }
                                placeholder={t(
                                    "filters.accountIdPlaceholder",
                                )}
                                className="
                                    w-full
                                    rounded-md
                                    border
                                    border-border
                                    bg-background
                                    px-3
                                    py-2
                                    text-sm
                                    text-foreground
                                    outline-none
                                "
                            />
                        </div>

                        <div>
                            <label
                                htmlFor="interest-posting-business-date"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "filters.businessDate",
                                )}
                            </label>

                            <input
                                id="interest-posting-business-date"
                                type="date"
                                value={
                                    filters.businessDate ??
                                    ""
                                }
                                onChange={
                                    handleBusinessDateChange
                                }
                                className="
                                    w-full
                                    rounded-md
                                    border
                                    border-border
                                    bg-background
                                    px-3
                                    py-2
                                    text-sm
                                    text-foreground
                                    outline-none
                                "
                            />
                        </div>

                        <div className="
                            flex
                            items-end
                        ">
                            <button
                                type="button"
                                onClick={
                                    handleClear
                                }
                                className="
                                    w-full
                                    rounded-md
                                    border
                                    border-border
                                    px-3
                                    py-2
                                    text-sm
                                    font-medium
                                    text-foreground
                                    transition
                                    hover:bg-background-subtle
                                "
                            >
                                {t(
                                    "filters.clear",
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

function getDefaultBusinessDate(): string {
    const date = new Date();

    date.setDate(
        date.getDate() - 1,
    );

    return date.toISOString().split("T")[0];
}