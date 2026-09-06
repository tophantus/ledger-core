"use client";

import {useTranslations} from "next-intl";

import {Link} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import type {AccountSummary} from "../types/account";

interface AccountCardProps {
    account: AccountSummary;
}

export function AccountCard({
                                account,
                            }: AccountCardProps) {
    const t = useTranslations("account");

    return (
        <Link
            href={ROUTES.ACCOUNT.DETAIL(account.id)}
            className="
                block
                rounded-lg
                border
                border-border
                bg-surface
                p-5
                shadow-sm
                transition
                hover:border-text-muted
                hover:shadow-md
            "
        >
            <div className="flex items-start justify-between gap-4">
                <div className="min-w-0">
                    <p className="text-xs text-text-muted">
                        {t("accountNumber")}
                    </p>

                    <p className="mt-1 truncate font-medium text-text-primary">
                        {account.accountNo}
                    </p>
                </div>

                <span
                    className="
                        shrink-0
                        rounded-full
                        bg-surface-subtle
                        px-2.5
                        py-1
                        text-xs
                        font-medium
                        text-text-secondary
                    "
                >
                    {account.currency}
                </span>
            </div>

            <div className="mt-6">
                <p className="text-xs text-text-muted">
                    {t("balance")}
                </p>

                <p className="mt-1 text-2xl font-semibold text-text-primary">
                    {account.balance} {account.currency}
                </p>
            </div>

            <div className="mt-5 border-t border-border pt-4">
                <p className="text-xs text-text-muted">
                    {t("status")}
                </p>

                <p className="mt-1 text-sm font-medium text-text-primary">
                    {t(`statuses.${account.status}`)}
                </p>
            </div>
        </Link>
    );
}