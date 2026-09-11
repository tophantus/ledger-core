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

import type {AccountSummary} from "@/features/account/types/account";

import type {
    WithdrawalIntentFilters,
    WithdrawalIntentStatus,
} from "../../types/withdrawal";

interface WithdrawalListFiltersProps {
    accounts: AccountSummary[];
    isAccountsLoading: boolean;
    filters: WithdrawalIntentFilters;
    onChange: (
        filters: WithdrawalIntentFilters,
    ) => void;
}

const WITHDRAWAL_STATUSES: WithdrawalIntentStatus[] = [
    "READY",
    "COMPLETED",
    "EXPIRED",
    "CANCELLED",
];

export function WithdrawalListFilters({
                                          accounts,
                                          isAccountsLoading,
                                          filters,
                                          onChange,
                                      }: WithdrawalListFiltersProps) {
    const t =
        useTranslations("withdrawal");

    const [expanded, setExpanded] =
        useState(false);

    const handleAccountChange = (
        event: ChangeEvent<HTMLSelectElement>,
    ) => {
        const value = event.target.value;

        onChange({
            ...filters,
            accountId:
                value || undefined,
            page: 0,
        });
    };

    const handleStatusChange = (
        event: ChangeEvent<HTMLSelectElement>,
    ) => {
        const value = event.target.value;

        onChange({
            ...filters,
            status:
                value
                    ? (
                        value as WithdrawalIntentStatus
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
                        (current) => !current,
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
                    {t("list.filter.title")}
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
                        {/* Account */}
                        <div>
                            <label
                                htmlFor="withdrawal-account"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "list.filter.account",
                                )}
                            </label>

                            <select
                                id="withdrawal-account"
                                value={
                                    filters.accountId
                                    ?? ""
                                }
                                disabled={
                                    isAccountsLoading
                                }
                                onChange={
                                    handleAccountChange
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
                                    disabled:cursor-not-allowed
                                    disabled:opacity-60
                                "
                            >
                                <option value="">
                                    {isAccountsLoading
                                        ? t(
                                            "list.filter.loadingAccounts",
                                        )
                                        : t(
                                            "list.filter.allAccounts",
                                        )}
                                </option>

                                {accounts.map(
                                    (account) => (
                                        <option
                                            key={
                                                account.id
                                            }
                                            value={
                                                account.id
                                            }
                                        >
                                            {
                                                account.accountNo
                                            }
                                            {" · "}
                                            {
                                                account.currency
                                            }
                                        </option>
                                    ),
                                )}
                            </select>
                        </div>

                        {/* Status */}
                        <div>
                            <label
                                htmlFor="withdrawal-status"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "list.filter.status",
                                )}
                            </label>

                            <select
                                id="withdrawal-status"
                                value={
                                    filters.status
                                    ?? ""
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
                                        "list.filter.allStatuses",
                                    )}
                                </option>

                                {WITHDRAWAL_STATUSES.map(
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
                                    "list.filter.clear",
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}