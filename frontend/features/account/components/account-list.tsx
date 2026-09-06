"use client";

import {useEffect, useState} from "react";
import {useTranslations} from "next-intl";

import {useMyAccounts} from "../hooks/use-my-accounts";
import {useAccountStore} from "../stores/account-store";

import {AccountCard} from "./account-card";
import {AccountCardSkeleton} from "./account-card-skeleton";
import {CreateAccountCard} from "@/features/account/components/create-account-card";
import {CreateAccountModal} from "@/features/account/components/create-account-modal";

export function AccountList() {
    const tErrors = useTranslations("errors");

    const {getMyAccounts} = useMyAccounts();

    const accounts = useAccountStore(
        (state) => state.accounts,
    );

    const [isLoading, setIsLoading] =
        useState(true);

    const [hasError, setHasError] =
        useState(false);

    const [errorMessage, setErrorMessage] =
        useState<string | null>(null);

    const [isCreateModalOpen, setIsCreateModalOpen] =
        useState(false);

    const handleCreateSuccess = () => {
        setIsCreateModalOpen(false);
    };

    useEffect(() => {
        let mounted = true;

        const loadAccounts = async () => {
            setIsLoading(true);
            setHasError(false);
            setErrorMessage(null);

            try {
                const response =
                    await getMyAccounts();

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
            } catch {
                if (mounted) {
                    setHasError(true);
                    setErrorMessage(
                        tErrors("fallback"),
                    );
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
    }, [getMyAccounts, tErrors]);

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
                <p className="text-sm text-danger">
                    {errorMessage ??
                        tErrors("fallback")}
                </p>
            </div>
        );
    }

    return (
        <>
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

                <CreateAccountCard
                    onClick={() =>
                        setIsCreateModalOpen(true)
                    }
                />
            </div>

            <CreateAccountModal
                open={isCreateModalOpen}
                onClose={() =>
                    setIsCreateModalOpen(false)
                }
                onSuccess={
                    handleCreateSuccess
                }
            />
        </>
    );
}