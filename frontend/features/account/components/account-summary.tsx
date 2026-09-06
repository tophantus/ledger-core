"use client";

import {useTranslations} from "next-intl";

import type {Account} from "../types/account";

interface AccountSummaryProps {
    account: Account;
}

export function AccountSummary({
                                   account,
                               }: AccountSummaryProps) {
    const t = useTranslations("account");

    return (
        <div className="rounded-lg border border-border bg-surface">
            <div className="flex flex-col gap-4 p-6 sm:flex-row sm:items-start sm:justify-between">
                <div>
                    <p className="text-xs text-text-muted">
                        {t("accountNumber")}
                    </p>

                    <p className="mt-1 text-lg font-semibold text-text-primary">
                        {account.accountNo}
                    </p>
                </div>

                <span className="w-fit rounded-full bg-surface-subtle px-2.5 py-1 text-xs font-medium text-text-secondary">
                    {t(`statuses.${account.status}`)}
                </span>
            </div>

            <div className="border-t border-border px-6 py-5">
                <p className="text-xs text-text-muted">
                    {t("balance")}
                </p>

                <p className="mt-1 text-2xl font-semibold text-text-primary">
                    {account.balance} {account.currency}
                </p>
            </div>
        </div>
    );
}