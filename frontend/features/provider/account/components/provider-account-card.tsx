"use client";

import {useLocale, useTranslations} from "next-intl";

import {Link} from "@/i18n/routing";
import {ProductBadge} from "@/features/product/components/product-badge";
import {getAccountStatusColor} from "@/lib/utils/account";
import {formatMoney} from "@/lib/utils/currency";
import {ROUTES} from "@/lib/constants/routes";

import type {ProviderAccountSummary} from "../types/provider-account";

interface ProviderAccountCardProps {
    account: ProviderAccountSummary;
}

export function ProviderAccountCard({
                                        account,
                                    }: ProviderAccountCardProps) {
    const t = useTranslations("provider.account");
    const locale = useLocale();

    return (
        <Link
            href={ROUTES.PROVIDER.ACCOUNT.DETAIL(
                account.id,
            )}
            className="
                group
                block
                rounded-xl
                border
                border-border
                bg-surface
                p-5
                shadow-sm
                transition-all
                hover:-translate-y-0.5
                hover:border-primary/30
                hover:shadow-md
            "
        >
            <div className="flex items-start justify-between gap-4">
                <div className="min-w-0">
                    <p className="text-xs font-medium text-text-muted">
                        {t("accountNumber")}
                    </p>

                    <p className="mt-1 truncate font-semibold text-text-primary">
                        {account.accountNo}
                    </p>

                    <div className="mt-2">
                        <ProductBadge
                            productId={account.productId}
                        />
                    </div>
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

            <div className="mt-7">
                <p className="text-xs font-medium text-text-muted">
                    {t("availableBalance")}
                </p>

                <p className="mt-1 text-2xl font-semibold tracking-tight text-text-primary">
                    {formatMoney(
                        account.availableBalance,
                        account.currency,
                        locale,
                    )}
                </p>
            </div>

            <div
                className="
                    mt-5
                    grid
                    grid-cols-2
                    gap-4
                    border-t
                    border-border
                    pt-4
                "
            >
                <div>
                    <p className="text-xs text-text-muted">
                        {t("balance")}
                    </p>

                    <p className="mt-1 text-sm font-medium text-text-primary">
                        {formatMoney(
                            account.balance,
                            account.currency,
                            locale,
                        )}
                    </p>
                </div>

                <div>
                    <p className="text-xs text-text-muted">
                        {t("holdAmount")}
                    </p>

                    <p className="mt-1 text-sm font-medium text-text-primary">
                        {formatMoney(
                            account.holdAmount,
                            account.currency,
                            locale,
                        )}
                    </p>
                </div>
            </div>

            <div className="mt-4 flex items-center justify-between">
                <span className="text-xs text-text-muted">
                    {t("status")}
                </span>

                <span
                    className={`
                        rounded-full
                        px-2.5
                        py-1
                        text-xs
                        font-medium
                        ${getAccountStatusColor(
                        account.status,
                        "text-background",
                    )}
                    `}
                >
                    {t(`statuses.${account.status}`)}
                </span>
            </div>
        </Link>
    );
}