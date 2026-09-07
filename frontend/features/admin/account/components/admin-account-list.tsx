"use client";

import {useTranslations} from "next-intl";

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
                        className="border-b border-border last:border-0"
                    >
                        <td className="px-4 py-3 font-medium">
                            {account.accountNo}
                        </td>

                        <td className="px-4 py-3">
                            {account.currency}
                        </td>

                        <td className="px-4 py-3">
                            {account.balance}
                        </td>

                        <td className="px-4 py-3">
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