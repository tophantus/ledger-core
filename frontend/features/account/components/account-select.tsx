"use client";

import type {ChangeEvent} from "react";
import {useLocale} from "next-intl";

import {formatMoney} from "@/lib/utils/currency";

import type {AccountSummary} from "../types/account";

interface AccountSelectProps {
    accounts: AccountSummary[];
    value: string;
    onChange: (
        event: ChangeEvent<HTMLSelectElement>,
    ) => void;
    disabled?: boolean;
    id?: string;
    placeholder: string;
    error?: string;
}

export function AccountSelect({
                                  accounts,
                                  value,
                                  onChange,
                                  disabled = false,
                                  id = "account",
                                  placeholder,
                                  error,
                              }: AccountSelectProps) {
    const locale = useLocale();

    return (
        <div>
            <select
                id={id}
                value={value}
                onChange={onChange}
                disabled={disabled}
                className="
                    mt-2
                    w-full
                    rounded-lg
                    border
                    border-border
                    bg-background
                    px-3
                    py-2.5
                    text-sm
                    text-text-primary
                    outline-none
                    transition
                    focus:ring-2
                    focus:ring-primary/20
                    disabled:cursor-not-allowed
                    disabled:opacity-60
                "
            >
                <option value="">
                    {placeholder}
                </option>

                {accounts.map((account) => (
                    <option
                        key={account.id}
                        value={account.id}
                    >
                        {account.accountNo} ·{" "}
                        {formatMoney(
                            account.availableBalance,
                            account.currency,
                            locale,
                        )}
                    </option>
                ))}
            </select>

            {error && (
                <p className="mt-1.5 text-sm text-danger">
                    {error}
                </p>
            )}
        </div>
    );
}