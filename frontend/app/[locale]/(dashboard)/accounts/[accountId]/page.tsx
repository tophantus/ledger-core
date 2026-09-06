"use client";

import {useEffect, useState} from "react";
import {useParams} from "next/navigation";
import {useTranslations} from "next-intl";

import {AccountDetails} from "@/features/account/components/account-details";
import {AccountDetailsSkeleton} from "@/features/account/components/account-details-skeleton";
import {useAccount} from "@/features/account/hooks/use-account";
import {useAccountStore} from "@/features/account/stores/account-store";
import {RecentTransactionList} from "@/features/transaction/components/recent-transaction-list";
import {useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

export default function AccountDetailsPage() {
    const t = useTranslations(
        "dashboard.account.details",
    );

    const router = useRouter();

    const params = useParams<{
        accountId: string;
    }>();

    const accountId = params.accountId;

    const {getAccount} = useAccount();

    const account = useAccountStore(
        (state) => state.currentAccount,
    );

    const setCurrentAccount =
        useAccountStore(
            (state) =>
                state.setCurrentAccount,
        );

    const clearCurrentAccount =
        useAccountStore(
            (state) =>
                state.clearCurrentAccount,
        );

    const [isLoading, setIsLoading] =
        useState(true);

    const [hasError, setHasError] =
        useState(false);

    useEffect(() => {
        let mounted = true;

        const loadAccount = async () => {
            try {
                const response =
                    await getAccount(accountId);

                if (!mounted) {
                    return;
                }

                if (!response.success) {
                    setHasError(true);
                    return;
                }

                setCurrentAccount(
                    response.data,
                );
            } catch {
                if (mounted) {
                    setHasError(true);
                }
            } finally {
                if (mounted) {
                    setIsLoading(false);
                }
            }
        };

        void loadAccount();

        return () => {
            mounted = false;
            clearCurrentAccount();
        };
    }, [
        accountId,
        getAccount,
        setCurrentAccount,
        clearCurrentAccount,
    ]);

    if (isLoading) {
        return (
            <section className="space-y-6">
                <AccountDetailsSkeleton />

                <div className="rounded-lg border border-border bg-surface p-6">
                    <div className="h-6 w-40 animate-pulse rounded bg-secondary" />
                </div>
            </section>
        );
    }

    if (hasError || !account) {
        return (
            <section className="rounded-lg border border-border bg-surface p-8 text-center">
                <p className="text-sm text-muted">
                    {t("loadError")}
                </p>
            </section>
        );
    }

    return (
        <section className="space-y-8">
            <AccountDetails
                onClosed={() =>
                    router.push(
                        ROUTES.DASHBOARD,
                    )
                }
            />

            <RecentTransactionList
                accountId={account.id}
            />
        </section>
    );
}