"use client";

import {useEffect, useState} from "react";
import {useTranslations} from "next-intl";

import {useMyAccounts} from "../hooks/use-my-accounts";
import {useAccountStore} from "../stores/account-store";

import {AccountCard} from "./account-card";
import {AccountCardSkeleton} from "./account-card-skeleton";

export function AccountList() {
    const t = useTranslations("account");

    const {getMyAccounts} = useMyAccounts();

    const accounts = useAccountStore(
        (state) => state.accounts,
    );

    const [isLoading, setIsLoading] =
        useState(true);

    const [hasError, setHasError] =
        useState(false);

    useEffect(() => {
        let mounted = true;

        const loadAccounts = async () => {
            setIsLoading(true);
            setHasError(false);

            try {
                const response =
                    await getMyAccounts();

                if (!mounted) {
                    return;
                }

                if (!response.success) {
                    setHasError(true);
                    return;
                }
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

        void loadAccounts();

        return () => {
            mounted = false;
        };
    }, [getMyAccounts]);

    if (isLoading) {
        return (
            <div
                className="
                    grid
                    gap-4
                    sm:grid-cols-2
                    lg:grid-cols-3
                "
            >
                {Array.from({length: 3}).map(
                    (_, index) => (
                        <AccountCardSkeleton
                            key={index}
                        />
                    ),
                )}
            </div>
        );
    }

    if (hasError) {
        return (
            <div
                className="
                    rounded-lg
                    border
                    border-border
                    bg-surface
                    p-6
                "
            >
                <p className="text-sm text-text-muted">
                    {t("loadError")}
                </p>
            </div>
        );
    }

    if (accounts.length === 0) {
        return (
            <div
                className="
                    rounded-lg
                    border
                    border-dashed
                    border-border
                    bg-surface
                    p-8
                    text-center
                "
            >
                <p className="text-sm text-text-muted">
                    {t("empty")}
                </p>
            </div>
        );
    }

    return (
        <div
            className="
                grid
                gap-4
                sm:grid-cols-2
                lg:grid-cols-3
            "
        >
            {accounts.map((account) => (
                <AccountCard
                    key={account.id}
                    account={account}
                />
            ))}
        </div>
    );
}