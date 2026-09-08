"use client";

import {ArrowLeft} from "lucide-react";
import {useLocale, useTranslations} from "next-intl";

import {Link} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import {AccountActions} from "@/features/account/components/account-actions";
import {ProductBadge} from "@/features/product/components/product-badge";
import {useAccountStore} from "../stores/account-store";

import {getAccountStatusColor} from "@/lib/utils/account";
import {formatMoney} from "@/lib/utils/currency";
import {ReactNode} from "react";

interface AccountDetailsProps {
    onClosed: () => void;
}

export function AccountDetails({
                                   onClosed,
                               }: AccountDetailsProps) {
    const t = useTranslations("account");
    const locale = useLocale();

    const account = useAccountStore(
        (state) => state.currentAccount,
    );

    if (!account) {
        return null;
    }

    return (
        <section className="space-y-6">
            {/* Back */}
            <Link
                href={ROUTES.DASHBOARD}
                className="
                    inline-flex
                    items-center
                    gap-2
                    text-sm
                    text-text-secondary
                    transition-colors
                    hover:text-text-primary
                "
            >
                <ArrowLeft className="h-4 w-4" />
                {t("details.back")}
            </Link>

            {/* Account */}
            <div
                className="
                    overflow-hidden
                    rounded-xl
                    border
                    border-border
                    bg-surface
                    shadow-sm
                "
            >
                {/* Header */}
                <div
                    className="
                        flex
                        items-start
                        justify-between
                        gap-4
                        border-b
                        border-border
                        px-6
                        py-5
                    "
                >
                    <div className="min-w-0">
                        <p className="text-xs font-medium text-text-muted">
                            {t("details.title")}
                        </p>

                        <h1 className="mt-1 truncate text-xl font-semibold tracking-tight text-text-primary">
                            {account.accountNo}
                        </h1>

                        <div className="mt-2">
                            <ProductBadge
                                productId={account.productId}
                            />
                        </div>
                    </div>

                    <div className="shrink-0">
                        <AccountActions
                            onClosed={onClosed}
                        />
                    </div>
                </div>

                {/* Balance */}
                <div
                    className="
                        grid
                        gap-6
                        border-b
                        border-border
                        p-6
                        sm:grid-cols-3
                    "
                >
                    <BalanceItem
                        label={t(
                            "details.availableBalance",
                        )}
                        value={formatMoney(
                            account.availableBalance,
                            account.currency,
                            locale,
                        )}
                        primary
                    />

                    <BalanceItem
                        label={t("details.balance")}
                        value={formatMoney(
                            account.balance,
                            account.currency,
                            locale,
                        )}
                    />

                    <BalanceItem
                        label={t("details.holdAmount")}
                        value={formatMoney(
                            account.holdAmount,
                            account.currency,
                            locale,
                        )}
                    />
                </div>

                {/* Information */}
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
                        label={t(
                            "details.accountNumber",
                        )}
                        value={account.accountNo}
                    />

                    <DetailItem
                        label={t("details.currency")}
                        value={account.currency}
                    />

                    <DetailItem
                        label={t("details.status")}
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
                        label={t("details.createdAt")}
                        value={formatDate(
                            account.createdAt,
                            locale,
                        )}
                    />

                    {/*<DetailItem*/}
                    {/*    label={t("details.updatedAt")}*/}
                    {/*    value={formatDate(*/}
                    {/*        account.updatedAt,*/}
                    {/*        locale,*/}
                    {/*    )}*/}
                    {/*/>*/}
                </div>
            </div>
        </section>
    );
}

interface BalanceItemProps {
    label: string;
    value: string;
    primary?: boolean;
}

function BalanceItem({
                         label,
                         value,
                         primary = false,
                     }: BalanceItemProps) {
    return (
        <div className="min-w-0">
            <p className="text-sm text-text-muted">
                {label}
            </p>

            <p
                className={`
                    mt-1
                    truncate
                    font-semibold
                    tracking-tight
                    ${
                    primary
                        ? "text-2xl text-text-primary"
                        : "text-lg text-text-primary"
                }
                `}
            >
                {value}
            </p>
        </div>
    );
}

interface DetailItemProps {
    label: string;
    value?: string;
    children?: ReactNode;
}

function DetailItem({
                        label,
                        value,
                        children,
                    }: DetailItemProps) {
    return (
        <div className="min-w-0 space-y-1">
            <p className="text-sm text-text-muted">
                {label}
            </p>

            {children ?? (
                <p className="break-all text-sm font-medium text-text-primary">
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