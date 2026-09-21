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
    WebhookFilters,
} from "../../types/webhook";
import {AccountSelect} from "@/features/account/components/account-select";

interface WebhookListFiltersProps {
    accounts: AccountSummary[];
    isAccountsLoading: boolean;
    filters: WebhookFilters;
    onChange: (
        filters: WebhookFilters,
    ) => void;
}

export function WebhookListFilters({
                                       accounts,
                                       isAccountsLoading,
                                       filters,
                                       onChange,
                                   }: WebhookListFiltersProps) {
    const t =
        useTranslations("webhook");

    const [expanded, setExpanded] =
        useState(false);

    const handleAccountChange = (
        event: ChangeEvent<HTMLSelectElement>,
    ) => {
        const value =
            event.target.value;

        onChange({
            ...filters,
            accountId:
                value || undefined,
            page: 0,
        });
    };

    const handleClear = () => {
        onChange({
            accountId: undefined,
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
                    ">
                        <div>
                            <label
                                htmlFor="webhook-account"
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

                            <AccountSelect
                                accounts={accounts}
                                value={filters.accountId ?? ""}
                                onChange={handleAccountChange}
                                disabled={isAccountsLoading}
                                id="webhook-account"
                                placeholder={
                                    isAccountsLoading
                                        ? t("list.filter.loadingAccounts")
                                        : t("list.filter.allAccounts")
                                }
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