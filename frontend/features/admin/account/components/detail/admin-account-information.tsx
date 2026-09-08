"use client";

import {useLocale, useTranslations} from "next-intl";
import type {ReactNode} from "react";

import {ProductBadge} from "@/features/product/components/product-badge";
import type {AdminAccountDetail} from "@/features/admin/account/types/admin-account";

import {getAccountStatusColor} from "@/lib/utils/account";
import {formatMoney} from "@/lib/utils/currency";

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
                    label={t("fields.product")}
                >
                    <ProductBadge
                        productId={account.productId}
                    />
                </DetailItem>

                <DetailItem
                    label={t("fields.currency")}
                    value={account.currency}
                />

                <DetailItem
                    label={t("fields.availableBalance")}
                    value={formatMoney(
                        account.availableBalance,
                        account.currency,
                        locale,
                    )}
                    valueClassName="text-lg"
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
                    label={t("fields.holdAmount")}
                    value={formatMoney(
                        account.holdAmount,
                        account.currency,
                        locale,
                    )}
                />

                <DetailItem
                    label={t("fields.status")}
                >
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
                </DetailItem>

                <DetailItem
                    label={t("fields.ledgerAccountId")}
                    value={account.ledgerAccountId}
                />

                <DetailItem
                    label={t("fields.createdAt")}
                    value={formatDate(
                        account.createdAt,
                        locale,
                    )}
                />

                {/*<DetailItem*/}
                {/*    label={t("fields.updatedAt")}*/}
                {/*    value={formatDate(*/}
                {/*        account.updatedAt,*/}
                {/*        locale,*/}
                {/*    )}*/}
                {/*/>*/}
            </div>
        </section>
    );
}

interface DetailItemProps {
    label: string;
    value?: string;
    valueClassName?: string;
    children?: ReactNode;
}

function DetailItem({
                        label,
                        value,
                        valueClassName = "",
                        children,
                    }: DetailItemProps) {
    return (
        <div className="min-w-0 space-y-1">
            <p className="text-sm text-muted">
                {label}
            </p>

            {children ?? (
                <p
                    className={`
                        break-all
                        text-sm
                        font-medium
                        text-primary
                        ${valueClassName}
                    `}
                >
                    {value}
                </p>
            )}
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