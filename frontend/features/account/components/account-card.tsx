"use client";

import {useLocale, useTranslations} from "next-intl";

import {Link} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import type {AccountSummary} from "../types/account";
import {getAccountStatusColor} from "@/lib/utils/account";

import {formatMoney} from "@/lib/utils/currency";
import {ProductBadge} from "@/features/product/components/product-badge";

interface AccountCardProps {
    account: AccountSummary;
}

export function AccountCard({
                                account,
                            }: AccountCardProps) {
    const t = useTranslations("account");
    const locale = useLocale();

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
                hover:bg-background-subtle
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

                    <ProductBadge
                        productId={account.productId}
                    />
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
                    {formatMoney(
                        account.balance,
                        account.currency,
                        locale,
                    )}
                </p>
            </div>

            <div className="mt-5 border-t border-border pt-4">
                <span className="text-xs text-muted">
                    {t("status")}{": "}
                </span>

                <span
                    className={`
                        mt-1
                        text-sm
                        font-medium
                        ${getAccountStatusColor(account.status, "text")}
                    `}
                >
                    {t(`statuses.${account.status}`)}
                </span>
                <div className={`w-full h-2 mt-2 rounded-full ${getAccountStatusColor(account.status, "background")}`}/>
            </div>
        </Link>
    );
}