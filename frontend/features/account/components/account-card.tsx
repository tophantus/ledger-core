"use client";

import {useTranslations} from "next-intl";

import {Link} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import type {AccountSummary} from "../types/account";
import {getAccountStatusColor} from "@/lib/utils/account";
import {useProductStore} from "@/features/product/store/product-store";
import {getProductColor} from "@/lib/utils/product";

interface AccountCardProps {
    account: AccountSummary;
}

export function AccountCard({
                                account,
                            }: AccountCardProps) {
    const t = useTranslations("account");
    const tProduct = useTranslations("product");


    const product = useProductStore(
        (state) =>
            state.productMap.get(account.productId),
    );

    if (!product) {
        return null;
    }

    const productName = tProduct(
        `names.${product.code}`,
    );

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

                    <div
                        className={`
                        shrink-0
                        rounded-full
                        px-2.5
                        py-1
                        text-xs
                        font-medium
                        ${getProductColor(product.code, "text")}
                        ${getProductColor(product.code, "background")}
                    `}
                    >
                        {productName}
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

            <div className="mt-6">
                <p className="text-xs text-text-muted">
                    {t("balance")}
                </p>

                <p className="mt-1 text-2xl font-semibold text-text-primary">
                    {account.balance} {account.currency}
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
                <div className={`w-full h-4 rounded-full ${getAccountStatusColor(account.status, "background")}`}/>
            </div>
        </Link>
    );
}