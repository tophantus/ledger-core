"use client";

import {useTranslations} from "next-intl";

import {useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

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
                    <th className="px-4 py-3">
                        {t("table.accountNo")}
                    </th>

                    <th className="px-4 py-3">
                        {t("table.currency")}
                    </th>

                    <th className="px-4 py-3">
                        {t("table.balance")}
                    </th>

                    <th className="px-4 py-3">
                        {t("table.status")}
                    </th>

                    <th className="px-4 py-3">
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
                                transition
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

                        <td className="px-4 py-3 text-primary">
                            {account.currency}
                        </td>

                        <td className="px-4 py-3 text-primary">
                            {account.balance}
                        </td>

                        <td className="px-4 py-3 text-primary">
                            {t(
                                `statuses.${account.status}`,
                            )}
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