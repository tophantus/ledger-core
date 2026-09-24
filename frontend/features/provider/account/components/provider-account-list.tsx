"use client";

import {useEffect, useState} from "react";
import {useTranslations} from "next-intl";

import {useGetProviderAccounts} from "../hooks/use-get-provider-accounts";
import type {ProviderAccountSummary} from "../types/provider-account";

import {ProviderAccountCard} from "./provider-account-card";
import {ProviderAccountCardSkeleton} from "./provider-account-card-skeleton";
import {ProviderAccountEmpty} from "./provider-account-empty";

export function ProviderAccountList() {
    const tErrors = useTranslations("errors");

    const {
        getProviderAccounts,
    } = useGetProviderAccounts();

    const [accounts, setAccounts] =
        useState<ProviderAccountSummary[]>([]);

    const [isLoading, setIsLoading] =
        useState(true);

    const [hasError, setHasError] =
        useState(false);

    const [errorMessage, setErrorMessage] =
        useState<string | null>(null);

    useEffect(() => {
        let mounted = true;

        const loadAccounts = async () => {
            setIsLoading(true);
            setHasError(false);
            setErrorMessage(null);

            try {
                const response =
                    await getProviderAccounts();

                if (!mounted) {
                    return;
                }

                if (!response.success) {
                    setHasError(true);

                    setErrorMessage(
                        response.code &&
                        tErrors.has(response.code)
                            ? tErrors(response.code)
                            : tErrors("fallback"),
                    );

                    return;
                }

                setAccounts(response.data);
            } catch {
                if (!mounted) {
                    return;
                }

                setHasError(true);
                setErrorMessage(
                    tErrors("fallback"),
                );
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
    }, [
        getProviderAccounts,
        tErrors,
    ]);

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
                        <ProviderAccountCardSkeleton
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
                <p className="text-sm text-danger">
                    {errorMessage ??
                        tErrors("fallback")}
                </p>
            </div>
        );
    }

    if (accounts.length === 0) {
        return <ProviderAccountEmpty />;
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
                <ProviderAccountCard
                    key={account.id}
                    account={account}
                />
            ))}
        </div>
    );
}