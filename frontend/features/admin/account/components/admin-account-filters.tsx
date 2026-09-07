"use client";

import {useTranslations} from "next-intl";

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
    text-primary
    outline-none
    placeholder:text-muted
    focus:border-primary
`;

export function AdminAccountFilters({
                                        filters,
                                        onChange,
                                    }: AdminAccountFiltersProps) {
    const t = useTranslations("admin.account");

    return (
        <div className="grid gap-4 md:grid-cols-3">
            <input
                type="text"
                value={filters.accountNo ?? ""}
                onChange={(event) =>
                    onChange({
                        ...filters,
                        accountNo:
                            event.target.value ||
                            undefined,
                        page: 0,
                    })
                }
                placeholder={t(
                    "filters.accountNoPlaceholder",
                )}
                className={inputClassName}
            />

            <select
                value={filters.status ?? ""}
                onChange={(event) =>
                    onChange({
                        ...filters,
                        status:
                            (event.target.value ||
                                undefined) as
                                | AccountStatus
                                | undefined,
                        page: 0,
                    })
                }
                className={inputClassName}
            >
                <option value="">
                    {t("filters.allStatuses")}
                </option>

                {ACCOUNT_STATUSES.map((status) => (
                    <option
                        key={status}
                        value={status}
                    >
                        {t(`statuses.${status}`)}
                    </option>
                ))}
            </select>

            <input
                type="text"
                value={filters.currency ?? ""}
                onChange={(event) =>
                    onChange({
                        ...filters,
                        currency:
                            event.target.value
                                .toUpperCase() ||
                            undefined,
                        page: 0,
                    })
                }
                placeholder={t(
                    "filters.currencyPlaceholder",
                )}
                maxLength={3}
                className={`${inputClassName} uppercase`}
            />
        </div>
    );
}