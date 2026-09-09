"use client";

import {useLocale, useTranslations} from "next-intl";

import {ProductBadge} from "@/features/product/components/product-badge";
import {useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import {getAccountStatusColor} from "@/lib/utils/account";
import {formatMoney} from "@/lib/utils/currency";

import type {AdminAccount} from "../types/admin-account";

interface AdminAccountListProps {
    accounts: AdminAccount[];
    loading: boolean;
}

export function AdminAccountList({
                                     accounts,
                                     loading,
                                 }: AdminAccountListProps) {
    const t = useTranslations("admin.account");
    const locale = useLocale();
    const router = useRouter();

    if (loading) {
        return (
            <div className="rounded-lg border border-border bg-surface p-6">
                {t("loading")}
            </div>
        );
    }

    if (accounts.length === 0) {
        return (
            <div className="rounded-lg border border-border bg-surface p-6 text-center text-muted">
                {t("empty")}
            </div>
        );
    }

    return (
        <div className="overflow-x-auto rounded-lg border border-border bg-surface">
            <table className="w-full text-sm">
                <thead className="border-b border-border">
                <tr className="text-left text-muted">
                    <th className="whitespace-nowrap px-4 py-3">
                        {t("table.accountNo")}
                    </th>

                    <th className="whitespace-nowrap px-4 py-3">
                        {t("table.product")}
                    </th>

                    <th className="whitespace-nowrap px-4 py-3">
                        {t("table.currency")}
                    </th>

                    <th className="whitespace-nowrap px-4 py-3">
                        {t("table.availableBalance")}
                    </th>

                    <th className="whitespace-nowrap px-4 py-3">
                        {t("table.balance")}
                    </th>

                    <th className="whitespace-nowrap px-4 py-3">
                        {t("table.holdAmount")}
                    </th>

                    <th className="whitespace-nowrap px-4 py-3">
                        {t("table.status")}
                    </th>

                    <th className="whitespace-nowrap px-4 py-3">
                        {t("table.userId")}
                    </th>
                </tr>
                </thead>

                <tbody>
                {accounts.map((account) => (
                    <tr
                        key={account.id}
                        tabIndex={0}
                        onClick={() =>
                            router.push(
                                ROUTES.ADMIN.ACCOUNT_DETAIL(
                                    account.id,
                                ),
                            )
                        }
                        onKeyDown={(event) => {
                            if (
                                event.key === "Enter" ||
                                event.key === " "
                            ) {
                                event.preventDefault();

                                router.push(
                                    ROUTES.ADMIN.ACCOUNT_DETAIL(
                                        account.id,
                                    ),
                                );
                            }
                        }}
                        className="
                            cursor-pointer
                            border-b
                            border-border
                            last:border-0
                            transition-colors
                            hover:bg-background
                            focus:outline-none
                            focus:ring-2
                            focus:ring-inset
                            focus:ring-primary
                        "
                    >
                        <td className="px-4 py-3 font-medium text-primary">
                            {account.accountNo}
                        </td>

                        <td className="px-4 py-3">
                            <ProductBadge
                                productId={account.productId}
                            />
                        </td>

                        <td className="px-4 py-3 text-primary">
                            {account.currency}
                        </td>

                        <td className="whitespace-nowrap px-4 py-3 font-medium text-primary">
                            {formatMoney(
                                account.availableBalance,
                                account.currency,
                                locale,
                            )}
                        </td>

                        <td className="whitespace-nowrap px-4 py-3 text-primary">
                            {formatMoney(
                                account.balance,
                                account.currency,
                                locale,
                            )}
                        </td>

                        <td className="whitespace-nowrap px-4 py-3 text-primary">
                            {formatMoney(
                                account.holdAmount,
                                account.currency,
                                locale,
                            )}
                        </td>

                        <td className="px-4 py-3">
                            <span
                                className={`
                                    inline-flex
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
                                {t(
                                    `statuses.${account.status}`,
                                )}
                            </span>
                        </td>

                        <td className="px-4 py-3 font-mono text-xs text-muted">
                            {account.userId}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}