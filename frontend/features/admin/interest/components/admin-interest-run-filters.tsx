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
    InterestRunFilters,
    InterestRunStatus,
    InterestRunType,
} from "../types/admin-interest";

interface AdminInterestRunFiltersProps {
    filters: InterestRunFilters;
    onChange: (
        filters: InterestRunFilters,
    ) => void;
}

const RUN_TYPES: InterestRunType[] = [
    "ACCRUAL",
    "POSTING",
];

const RUN_STATUSES: InterestRunStatus[] = [
    "PENDING",
    "RUNNING",
    "COMPLETED",
];

export function AdminInterestRunFilters({
                                            filters,
                                            onChange,
                                        }: AdminInterestRunFiltersProps) {
    const t = useTranslations(
        "admin.interest.runs",
    );

    const [expanded, setExpanded] =
        useState(false);

    const handleBusinessDateChange = (
        event: ChangeEvent<HTMLInputElement>,
    ) => {
        const value =
            event.target.value;

        onChange({
            ...filters,
            businessDate:
                value || undefined,
            page: 0,
        });
    };

    const handleFromDateChange = (
        event: ChangeEvent<HTMLInputElement>,
    ) => {
        const value =
            event.target.value;

        if (!value) {
            onChange({
                ...filters,
                fromDate: undefined,
                page: 0,
            });

            return;
        }

        if (
            filters.toDate &&
            value > filters.toDate
        ) {
            return;
        }

        onChange({
            ...filters,
            fromDate: value,
            page: 0,
        });
    };

    const handleToDateChange = (
        event: ChangeEvent<HTMLInputElement>,
    ) => {
        const value =
            event.target.value;

        if (!value) {
            onChange({
                ...filters,
                toDate: undefined,
                page: 0,
            });

            return;
        }

        if (
            filters.fromDate &&
            value < filters.fromDate
        ) {
            return;
        }

        onChange({
            ...filters,
            toDate: value,
            page: 0,
        });
    };

    const handleRunTypeChange = (
        event: ChangeEvent<HTMLSelectElement>,
    ) => {
        const value =
            event.target.value;

        onChange({
            ...filters,
            runType: value
                ? (
                    value as InterestRunType
                )
                : undefined,
            page: 0,
        });
    };

    const handleStatusChange = (
        event: ChangeEvent<HTMLSelectElement>,
    ) => {
        const value =
            event.target.value;

        onChange({
            ...filters,
            status: value
                ? (
                    value as InterestRunStatus
                )
                : undefined,
            page: 0,
        });
    };

    const handleClear = () => {
        onChange({
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
                        lg:grid-cols-3
                    ">
                        {/* Business Date */}
                        <div>
                            <label
                                htmlFor="interest-run-business-date"
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
                                id="interest-run-business-date"
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

                        {/* From Date */}
                        <div>
                            <label
                                htmlFor="interest-run-from-date"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "filters.fromDate",
                                )}
                            </label>

                            <input
                                id="interest-run-from-date"
                                type="date"
                                value={
                                    filters.fromDate ??
                                    ""
                                }
                                max={
                                    filters.toDate
                                }
                                onChange={
                                    handleFromDateChange
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

                        {/* To Date */}
                        <div>
                            <label
                                htmlFor="interest-run-to-date"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "filters.toDate",
                                )}
                            </label>

                            <input
                                id="interest-run-to-date"
                                type="date"
                                value={
                                    filters.toDate ??
                                    ""
                                }
                                min={
                                    filters.fromDate
                                }
                                onChange={
                                    handleToDateChange
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

                        {/* Run Type */}
                        <div>
                            <label
                                htmlFor="interest-run-type"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "filters.runType",
                                )}
                            </label>

                            <select
                                id="interest-run-type"
                                value={
                                    filters.runType ??
                                    ""
                                }
                                onChange={
                                    handleRunTypeChange
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
                            >
                                <option value="">
                                    {t(
                                        "filters.allRunTypes",
                                    )}
                                </option>

                                {RUN_TYPES.map(
                                    (runType) => (
                                        <option
                                            key={
                                                runType
                                            }
                                            value={
                                                runType
                                            }
                                        >
                                            {t(
                                                `runTypes.${runType}`,
                                            )}
                                        </option>
                                    ),
                                )}
                            </select>
                        </div>

                        {/* Status */}
                        <div>
                            <label
                                htmlFor="interest-run-status"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "filters.status",
                                )}
                            </label>

                            <select
                                id="interest-run-status"
                                value={
                                    filters.status ??
                                    ""
                                }
                                onChange={
                                    handleStatusChange
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
                            >
                                <option value="">
                                    {t(
                                        "filters.allStatuses",
                                    )}
                                </option>

                                {RUN_STATUSES.map(
                                    (status) => (
                                        <option
                                            key={
                                                status
                                            }
                                            value={
                                                status
                                            }
                                        >
                                            {t(
                                                `statuses.${status}`,
                                            )}
                                        </option>
                                    ),
                                )}
                            </select>
                        </div>

                        {/* Clear */}
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