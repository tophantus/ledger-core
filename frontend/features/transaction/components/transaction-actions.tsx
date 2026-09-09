"use client";

import {
    ArrowDownToLine,
    ArrowRightLeft,
} from "lucide-react";
import {useTranslations} from "next-intl";

import {Link} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";
import {useAccountStore} from "@/features/account/stores/account-store";
import {isAmountGreaterThanZero} from "@/lib/utils/money";

export function TransactionActions() {
    const t = useTranslations("transaction");

    const accounts = useAccountStore(
        (state) => state.accounts,
    );

    const hasAvailableAccount = accounts.some(
        (account) =>
            isAmountGreaterThanZero(
                account.availableBalance,
            ),
    );

    if (!hasAvailableAccount) {
        return null;
    }

    return (
        <div className="rounded-lg border border-border bg-surface p-5">
            <h2 className="text-base font-semibold text-text-primary">
                {t("actions.title")}
            </h2>

            <div className="mt-4 grid gap-3 sm:grid-cols-2">
                <Link
                    href={ROUTES.TRANSACTION.TRANSFER}
                    className="
                        flex
                        items-center
                        gap-3
                        rounded-lg
                        border
                        border-border
                        bg-background
                        p-4
                        transition
                        hover:border-primary
                        hover:bg-background-subtle
                    "
                >
                    <div
                        className="
                            flex
                            h-10
                            w-10
                            shrink-0
                            items-center
                            justify-center
                            rounded-full
                            bg-primary
                            text-primary-foreground
                        "
                    >
                        <ArrowRightLeft className="h-5 w-5" />
                    </div>

                    <div className="min-w-0">
                        <p className="text-sm font-medium text-primary">
                            {t("actions.transfer")}
                        </p>

                        <p className="mt-1 text-xs text-muted">
                            {t("actions.transferDescription")}
                        </p>
                    </div>
                </Link>

                <Link
                    href={ROUTES.TRANSACTION.WITHDRAW}
                    className="
                        flex
                        items-center
                        gap-3
                        rounded-lg
                        border
                        border-border
                        bg-background
                        p-4
                        transition
                        hover:border-primary
                        hover:bg-background-subtle
                    "
                >
                    <div
                        className="
                            flex
                            h-10
                            w-10
                            shrink-0
                            items-center
                            justify-center
                            rounded-full
                            bg-secondary
                            text-secondary-foreground
                        "
                    >
                        <ArrowDownToLine className="h-5 w-5" />
                    </div>

                    <div className="min-w-0">
                        <p className="text-sm font-medium text-secondary">
                            {t("actions.withdraw")}
                        </p>

                        <p className="mt-1 text-xs text-muted">
                            {t("actions.withdrawDescription")}
                        </p>
                    </div>
                </Link>
            </div>
        </div>
    );
}