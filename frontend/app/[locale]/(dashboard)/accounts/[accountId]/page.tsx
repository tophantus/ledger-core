"use client";

import {useEffect, useState} from "react";
import {useParams} from "next/navigation";
import {useTranslations} from "next-intl";

import {AccountDetails} from "@/features/account/components/account-details";
import {AccountDetailsSkeleton} from "@/features/account/components/account-details-skeleton";
import {useAccount} from "@/features/account/hooks/use-account";
import {RecentTransactionList} from "@/features/transaction/components/recent-transaction-list";
import type {Account} from "@/features/account/types/account";

export default function AccountDetailsPage() {
    const t = useTranslations(
        "dashboard.account.details",
    );

    const params = useParams<{
        accountId: string;
    }>();

    const accountId = params.accountId;

    const {getAccount} = useAccount();

    const [account, setAccount] =
        useState<Account | null>(null);

    const [isLoading, setIsLoading] =
        useState(true);

    const [hasError, setHasError] =
        useState(false);

    useEffect(() => {
        let mounted = true;

        const loadAccount = async () => {
            setIsLoading(true);
            setHasError(false);

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

                setAccount(response.data);
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
        };
    }, [accountId, getAccount]);

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
                <p className="text-sm text-text-muted">
                    {t("loadError")}
                </p>
            </section>
        );
    }

    return (
        <section className="space-y-8">
            <AccountDetails account={account} />

            <RecentTransactionList
                accountId={account.id}
            />
        </section>
    );
}