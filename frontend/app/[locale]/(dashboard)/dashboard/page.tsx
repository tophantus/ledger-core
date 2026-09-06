"use client";

import {useTranslations} from "next-intl";

import {AccountList} from "@/features/account/components/account-list";
import {useUserStore} from "@/features/user/stores/user-store";
import {TransactionActions} from "@/features/transaction/components/transaction-actions";

export default function DashboardPage() {
    const t = useTranslations("dashboard");

    const currentUser = useUserStore(
        (state) => state.currentUser,
    );

    return (
        <section className="space-y-6">
            <div>
                <h1 className="text-2xl font-semibold text-text-primary">
                    {t("title")}
                </h1>

                <p className="mt-1 text-sm text-text-muted">
                    {t("welcome", {
                        name:
                            currentUser?.profile.fullName ?? "",
                    })}
                </p>
            </div>

            <TransactionActions />

            <AccountList />
        </section>
    );
}