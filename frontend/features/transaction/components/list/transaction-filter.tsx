"use client";

import {
    useState,
    type ChangeEvent,
} from "react";
import {ChevronDown, ChevronUp} from "lucide-react";
import {useTranslations} from "next-intl";

import type {AccountSummary} from "@/features/account/types/account";
import {SUPPORTED_CURRENCIES} from "@/lib/constants/currency";

import type {
    TransactionFilters,
    TransactionStatus,
    TransactionType,
} from "../../types/transaction";

interface TransactionFilterProps {
    accounts: AccountSummary[];
    isAccountsLoading: boolean;
    filters: TransactionFilters;
    onChange: (
        filters: TransactionFilters,
    ) => void;
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
                                      accounts,
                                      isAccountsLoading,
                                      filters,
                                      onChange,
                                  }: TransactionFilterProps) {
    const t = useTranslations("transaction");

    const today = getTodayLocal();

    const [expanded, setExpanded] =
        useState(false);

    const fromDate = filters.from
        ? toDateLocal(filters.from)
        : undefined;

    const toDate = filters.to
        ? toDateLocal(filters.to)
        : undefined;

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

    const handleTypeChange = (
        event: ChangeEvent<HTMLSelectElement>,
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
        event: ChangeEvent<HTMLSelectElement>,
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
        event: ChangeEvent<HTMLSelectElement>,
    ) => {
        const value = event.target.value;

        onChange({
            ...filters,
            currency:
                value || undefined,
            page: 0,
        });
    };

    const handleFromChange = (
        event: ChangeEvent<HTMLInputElement>,
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

        if (
            toDate &&
            selectedDate >
            new Date(
                `${toDate}T00:00:00`,
            )
        ) {
            return;
        }

        onChange({
            ...filters,
            from:
                selectedDate.toISOString(),
            page: 0,
        });
    };

    const handleToChange = (
        event: ChangeEvent<HTMLInputElement>,
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
            new Date(
                `${fromDate}T00:00:00`,
            )
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
                        {/* Account */}
                        <div>
                            <label
                                htmlFor="transaction-account"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "filters.account",
                                )}
                            </label>

                            <select
                                id="transaction-account"
                                value={
                                    filters.accountId
                                    ?? ""
                                }
                                onChange={
                                    handleAccountChange
                                }
                                disabled={
                                    isAccountsLoading
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
                                            "filters.loadingAccounts",
                                        )
                                        : t(
                                            "filters.allAccounts",
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
                                            }{" "}
                                            ·{" "}
                                            {
                                                account.currency
                                            }
                                        </option>
                                    ),
                                )}
                            </select>
                        </div>

                        {/* Type */}
                        <div>
                            <label
                                htmlFor="transaction-type"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "filters.type",
                                )}
                            </label>

                            <select
                                id="transaction-type"
                                value={
                                    filters.type
                                    ?? ""
                                }
                                onChange={
                                    handleTypeChange
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
                                        "filters.allTypes",
                                    )}
                                </option>

                                {TRANSACTION_TYPES.map(
                                    (type) => (
                                        <option
                                            key={type}
                                            value={type}
                                        >
                                            {t(
                                                `types.${type}`,
                                            )}
                                        </option>
                                    ),
                                )}
                            </select>
                        </div>

                        {/*/!* Status *!/*/}
                        {/*<div>*/}
                        {/*    <label*/}
                        {/*        htmlFor="transaction-status"*/}
                        {/*        className="*/}
                        {/*            mb-2*/}
                        {/*            block*/}
                        {/*            text-sm*/}
                        {/*            font-medium*/}
                        {/*            text-foreground*/}
                        {/*        "*/}
                        {/*    >*/}
                        {/*        {t(*/}
                        {/*            "filters.status",*/}
                        {/*        )}*/}
                        {/*    </label>*/}

                        {/*    <select*/}
                        {/*        id="transaction-status"*/}
                        {/*        value={*/}
                        {/*            filters.status*/}
                        {/*            ?? ""*/}
                        {/*        }*/}
                        {/*        onChange={*/}
                        {/*            handleStatusChange*/}
                        {/*        }*/}
                        {/*        className="*/}
                        {/*            w-full*/}
                        {/*            rounded-md*/}
                        {/*            border*/}
                        {/*            border-border*/}
                        {/*            bg-background*/}
                        {/*            px-3*/}
                        {/*            py-2*/}
                        {/*            text-sm*/}
                        {/*            text-foreground*/}
                        {/*            outline-none*/}
                        {/*        "*/}
                        {/*    >*/}
                        {/*        <option value="">*/}
                        {/*            {t(*/}
                        {/*                "filters.allStatuses",*/}
                        {/*            )}*/}
                        {/*        </option>*/}

                        {/*        {TRANSACTION_STATUSES.map(*/}
                        {/*            (status) => (*/}
                        {/*                <option*/}
                        {/*                    key={*/}
                        {/*                        status*/}
                        {/*                    }*/}
                        {/*                    value={*/}
                        {/*                        status*/}
                        {/*                    }*/}
                        {/*                >*/}
                        {/*                    {t(*/}
                        {/*                        `statuses.${status}`,*/}
                        {/*                    )}*/}
                        {/*                </option>*/}
                        {/*            ),*/}
                        {/*        )}*/}
                        {/*    </select>*/}
                        {/*</div>*/}

                        {/* Currency */}
                        <div>
                            <label
                                htmlFor="transaction-currency"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "filters.currency",
                                )}
                            </label>

                            <select
                                id="transaction-currency"
                                value={
                                    filters.currency
                                    ?? ""
                                }
                                onChange={
                                    handleCurrencyChange
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
                                        "filters.allCurrencies",
                                    )}
                                </option>

                                {SUPPORTED_CURRENCIES.map(
                                    (currency) => (
                                        <option
                                            key={
                                                currency
                                            }
                                            value={
                                                currency
                                            }
                                        >
                                            {currency}
                                        </option>
                                    ),
                                )}
                            </select>
                        </div>

                        {/* From */}
                        <div>
                            <label
                                htmlFor="transaction-from"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "filters.from",
                                )}
                            </label>

                            <input
                                id="transaction-from"
                                type="date"
                                value={
                                    fromDate ?? ""
                                }
                                max={
                                    toDate ?? today
                                }
                                onChange={
                                    handleFromChange
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

                        {/* To */}
                        <div>
                            <label
                                htmlFor="transaction-to"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "filters.to",
                                )}
                            </label>

                            <input
                                id="transaction-to"
                                type="date"
                                value={
                                    toDate ?? ""
                                }
                                min={fromDate}
                                max={today}
                                onChange={
                                    handleToChange
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

function getTodayLocal(): string {
    const date = new Date();

    const year =
        date.getFullYear();

    const month = String(
        date.getMonth() + 1,
    ).padStart(2, "0");

    const day = String(
        date.getDate(),
    ).padStart(2, "0");

    return `${year}-${month}-${day}`;
}

function toDateLocal(
    value: string,
): string {
    const date = new Date(value);

    const year =
        date.getFullYear();

    const month = String(
        date.getMonth() + 1,
    ).padStart(2, "0");

    const day = String(
        date.getDate(),
    ).padStart(2, "0");

    return `${year}-${month}-${day}`;
}