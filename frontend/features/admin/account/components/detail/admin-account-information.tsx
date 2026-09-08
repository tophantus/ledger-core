"use client";

import {useLocale, useTranslations} from "next-intl";

import {formatMoney} from "@/lib/utils/currency";
import {AdminAccountDetail} from "@/features/admin/account/types/admin-account";


interface Props {
    account: AdminAccountDetail;
}

export function AdminAccountInformation({
                                            account,
                                        }: Props) {
    const t = useTranslations(
        "admin.account.detail",
    );

    const locale = useLocale();

    return (
        <section
            className="
                rounded-lg
                border
                border-border
                bg-surface
            "
        >
            <div className="border-b border-border px-6 py-4">
                <h2 className="font-semibold text-primary">
                    {t("accountInformation")}
                </h2>
            </div>

            <div
                className="
                    grid
                    gap-6
                    p-6
                    sm:grid-cols-2
                    lg:grid-cols-3
                "
            >
                <DetailItem
                    label={t("fields.accountNo")}
                    value={account.accountNo}
                />

                <DetailItem
                    label={t("fields.currency")}
                    value={account.currency}
                />

                <DetailItem
                    label={t("fields.balance")}
                    value={formatMoney(
                        account.balance,
                        account.currency,
                        locale,
                    )}
                />

                <DetailItem
                    label={t("fields.status")}
                    value={t(
                        `statuses.${account.status}`,
                    )}
                />

                <DetailItem
                    label={t(
                        "fields.ledgerAccountId",
                    )}
                    value={account.ledgerAccountId}
                />

                <DetailItem
                    label={t("fields.createdAt")}
                    value={formatDate(
                        account.createdAt,
                        locale,
                    )}
                />

                <DetailItem
                    label={t("fields.updatedAt")}
                    value={formatDate(
                        account.updatedAt,
                        locale,
                    )}
                />
            </div>
        </section>
    );
}

interface DetailItemProps {
    label: string;
    value: string;
}

function DetailItem({
                        label,
                        value,
                    }: DetailItemProps) {
    return (
        <div className="min-w-0 space-y-1">
            <p className="text-sm text-muted">
                {label}
            </p>

            <p className="break-all text-sm font-medium text-primary">
                {value}
            </p>
        </div>
    );
}

function formatDate(
    value: string,
    locale: string,
): string {
    return new Date(value).toLocaleString(
        locale,
    );
}