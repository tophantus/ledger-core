"use client";

import {
    type ChangeEvent,
} from "react";
import {useTranslations} from "next-intl";

import {
    getCurrency,
    SUPPORTED_CURRENCIES,
} from "@/lib/constants/currency";

import type {
    AccountStatus,
    AdminAccountFilters as AdminAccountFilterValues,
} from "../types/admin-account";

interface AdminAccountFiltersProps {
    filters: AdminAccountFilterValues;
    onChange: (
        filters: AdminAccountFilterValues,
    ) => void;
}

const ACCOUNT_STATUSES: AccountStatus[] = [
    "ACTIVE",
    "BLOCKED",
    "CLOSED",
];

const inputClassName = `
    h-10
    w-full
    rounded-md
    border
    border-border
    bg-surface
    px-3
    text-sm
    text-foreground
    outline-none
    placeholder:text-muted
    focus:border-primary
`;

function getAccountStatus(
    value: string,
): AccountStatus | undefined {
    if (
        value === "ACTIVE" ||
        value === "BLOCKED" ||
        value === "CLOSED"
    ) {
        return value;
    }

    return undefined;
}

export function AdminAccountFilters({
                                        filters,
                                        onChange,
                                    }: AdminAccountFiltersProps) {
    const t = useTranslations(
        "admin.account",
    );

    const handleAccountNoChange = (
        event: ChangeEvent<HTMLInputElement>,
    ) => {
        onChange({
            ...filters,
            accountNo:
                event.target.value ||
                undefined,
            page: 0,
        });
    };

    const handleStatusChange = (
        event: ChangeEvent<HTMLSelectElement>,
    ) => {
        onChange({
            ...filters,
            status: getAccountStatus(
                event.target.value,
            ),
            page: 0,
        });
    };

    const handleCurrencyChange = (
        event: ChangeEvent<HTMLSelectElement>,
    ) => {
        onChange({
            ...filters,
            currency: getCurrency(
                event.target.value,
            ),
            page: 0,
        });
    };

    return (
        <div className="
            grid
            gap-4
            md:grid-cols-3
        ">
            <input
                type="text"
                value={
                    filters.accountNo ?? ""
                }
                onChange={
                    handleAccountNoChange
                }
                placeholder={t(
                    "filters.accountNoPlaceholder",
                )}
                className={inputClassName}
            />

            <select
                value={
                    filters.status ?? ""
                }
                onChange={
                    handleStatusChange
                }
                className={inputClassName}
            >
                <option value="">
                    {t(
                        "filters.allStatuses",
                    )}
                </option>

                {ACCOUNT_STATUSES.map(
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

            <select
                value={
                    filters.currency ?? ""
                }
                onChange={
                    handleCurrencyChange
                }
                className={inputClassName}
            >
                <option value="">
                    {t(
                        "filters.allCurrencies",
                    )}
                </option>

                {SUPPORTED_CURRENCIES.map(
                    (currency) => (
                        <option
                            key={currency}
                            value={currency}
                        >
                            {currency}
                        </option>
                    ),
                )}
            </select>
        </div>
    );
}