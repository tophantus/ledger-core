"use client";

import {useTranslations} from "next-intl";

import type {
    TransactionFilters,
    TransactionStatus,
    TransactionType,
} from "../types/transaction";

interface TransactionFilterProps {
    filters: TransactionFilters;
    onChange: (filters: TransactionFilters) => void;
}

const TRANSACTION_TYPES: TransactionType[] = [
    "TRANSFER",
    "DEPOSIT",
    "WITHDRAW",
    "FEE",
    "REFUND",
];

const TRANSACTION_STATUSES: TransactionStatus[] = [
    "PENDING",
    "COMPLETED",
    "FAILED",
    "CANCELLED",
];

export function TransactionFilter({
                                      filters,
                                      onChange,
                                  }: TransactionFilterProps) {
    const t = useTranslations("transaction");

    const today = getTodayLocal();

    const fromDate = filters.from
        ? toDateLocal(filters.from)
        : undefined;

    const toDate = filters.to
        ? toDateLocal(filters.to)
        : undefined;

    const handleTypeChange = (
        event: React.ChangeEvent<HTMLSelectElement>,
    ) => {
        const value = event.target.value;

        onChange({
            ...filters,
            type: value
                ? (value as TransactionType)
                : undefined,
            page: 0,
        });
    };

    const handleStatusChange = (
        event: React.ChangeEvent<HTMLSelectElement>,
    ) => {
        const value = event.target.value;

        onChange({
            ...filters,
            status: value
                ? (value as TransactionStatus)
                : undefined,
            page: 0,
        });
    };

    const handleCurrencyChange = (
        event: React.ChangeEvent<HTMLInputElement>,
    ) => {
        const value = event.target.value
            .trim()
            .toUpperCase();

        onChange({
            ...filters,
            currency: value || undefined,
            page: 0,
        });
    };

    const handleFromChange = (
        event: React.ChangeEvent<HTMLInputElement>,
    ) => {
        const value = event.target.value;

        if (!value) {
            onChange({
                ...filters,
                from: undefined,
                page: 0,
            });

            return;
        }

        const selectedDate = new Date(
            `${value}T00:00:00`,
        );

        const todayDate = new Date(
            `${today}T00:00:00`,
        );

        if (selectedDate > todayDate) {
            return;
        }

        if (toDate && selectedDate > new Date(`${toDate}T00:00:00`)) {
            return;
        }

        onChange({
            ...filters,
            from: selectedDate.toISOString(),
            page: 0,
        });
    };

    const handleToChange = (
        event: React.ChangeEvent<HTMLInputElement>,
    ) => {
        const value = event.target.value;

        if (!value) {
            onChange({
                ...filters,
                to: undefined,
                page: 0,
            });

            return;
        }

        const selectedDate = new Date(
            `${value}T23:59:59.999`,
        );

        const todayDate = new Date(
            `${today}T23:59:59.999`,
        );

        if (selectedDate > todayDate) {
            return;
        }

        if (
            fromDate &&
            selectedDate <
            new Date(`${fromDate}T00:00:00`)
        ) {
            return;
        }

        onChange({
            ...filters,
            to: selectedDate.toISOString(),
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
        <div className="rounded-lg border border-border bg-surface p-4">
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                <div>
                    <label
                        htmlFor="transaction-type"
                        className="mb-2 block text-sm font-medium text-text-primary"
                    >
                        {t("filters.type")}
                    </label>

                    <select
                        id="transaction-type"
                        value={filters.type ?? ""}
                        onChange={handleTypeChange}
                        className="
                            w-full
                            rounded-md
                            border
                            border-border
                            bg-background
                            px-3
                            py-2
                            text-sm
                            text-text-primary
                            outline-none
                            focus:border-primary
                        "
                    >
                        <option value="">
                            {t("filters.allTypes")}
                        </option>

                        {TRANSACTION_TYPES.map((type) => (
                            <option
                                key={type}
                                value={type}
                            >
                                {t(`types.${type}`)}
                            </option>
                        ))}
                    </select>
                </div>

                <div>
                    <label
                        htmlFor="transaction-status"
                        className="mb-2 block text-sm font-medium text-text-primary"
                    >
                        {t("filters.status")}
                    </label>

                    <select
                        id="transaction-status"
                        value={filters.status ?? ""}
                        onChange={handleStatusChange}
                        className="
                            w-full
                            rounded-md
                            border
                            border-border
                            bg-background
                            px-3
                            py-2
                            text-sm
                            text-text-primary
                            outline-none
                            focus:border-primary
                        "
                    >
                        <option value="">
                            {t("filters.allStatuses")}
                        </option>

                        {TRANSACTION_STATUSES.map(
                            (status) => (
                                <option
                                    key={status}
                                    value={status}
                                >
                                    {t(
                                        `statuses.${status}`,
                                    )}
                                </option>
                            ),
                        )}
                    </select>
                </div>

                <div>
                    <label
                        htmlFor="transaction-currency"
                        className="mb-2 block text-sm font-medium text-text-primary"
                    >
                        {t("filters.currency")}
                    </label>

                    <input
                        id="transaction-currency"
                        type="text"
                        maxLength={3}
                        value={filters.currency ?? ""}
                        onChange={handleCurrencyChange}
                        placeholder="VND"
                        className="
                            w-full
                            rounded-md
                            border
                            border-border
                            bg-background
                            px-3
                            py-2
                            text-sm
                            uppercase
                            text-text-primary
                            outline-none
                            placeholder:text-text-muted
                            focus:border-primary
                        "
                    />
                </div>

                <div>
                    <label
                        htmlFor="transaction-from"
                        className="mb-2 block text-sm font-medium text-text-primary"
                    >
                        {t("filters.from")}
                    </label>

                    <input
                        id="transaction-from"
                        type="date"
                        value={fromDate ?? ""}
                        max={toDate ?? today}
                        onChange={handleFromChange}
                        className="
                            w-full
                            rounded-md
                            border
                            border-border
                            bg-background
                            px-3
                            py-2
                            text-sm
                            text-text-primary
                            outline-none
                            focus:border-primary
                        "
                    />
                </div>

                <div>
                    <label
                        htmlFor="transaction-to"
                        className="mb-2 block text-sm font-medium text-text-primary"
                    >
                        {t("filters.to")}
                    </label>

                    <input
                        id="transaction-to"
                        type="date"
                        value={toDate ?? ""}
                        min={fromDate}
                        max={today}
                        onChange={handleToChange}
                        className="
                            w-full
                            rounded-md
                            border
                            border-border
                            bg-background
                            px-3
                            py-2
                            text-sm
                            text-text-primary
                            outline-none
                            focus:border-primary
                        "
                    />
                </div>

                <div className="flex items-end">
                    <button
                        type="button"
                        onClick={handleClear}
                        className="
                            w-full
                            rounded-md
                            border
                            border-border
                            px-3
                            py-2
                            text-sm
                            font-medium
                            text-text-primary
                            transition
                            hover:bg-secondary
                        "
                    >
                        {t("filters.clear")}
                    </button>
                </div>
            </div>
        </div>
    );
}

function getTodayLocal(): string {
    const date = new Date();

    const year = date.getFullYear();
    const month = String(
        date.getMonth() + 1,
    ).padStart(2, "0");
    const day = String(
        date.getDate(),
    ).padStart(2, "0");

    return `${year}-${month}-${day}`;
}

function toDateLocal(value: string): string {
    const date = new Date(value);

    const year = date.getFullYear();
    const month = String(
        date.getMonth() + 1,
    ).padStart(2, "0");
    const day = String(
        date.getDate(),
    ).padStart(2, "0");

    return `${year}-${month}-${day}`;
}