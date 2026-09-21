"use client";

import {create} from "zustand";

import type {
    Account,
    AccountStatus,
    AccountSummary,
} from "../types/account";

interface AccountState {
    accounts: AccountSummary[];
    initialized: boolean;
    currentAccount: Account | null;

    setAccounts: (
        accounts: AccountSummary[],
    ) => void;

    setCurrentAccount: (
        account: Account,
    ) => void;

    clearCurrentAccount: () => void;

    addAccount: (
        account: AccountSummary,
    ) => void;

    removeAccount: (
        accountId: string,
    ) => void;

    updateAccountStatus: (
        accountId: string,
        status: AccountStatus,
    ) => void;
}

export const useAccountStore =
    create<AccountState>((set) => ({
        accounts: [],
        initialized: false,
        currentAccount: null,

        setAccounts: (accounts) =>
            set({
                accounts,
                initialized: true,
            }),

        setCurrentAccount: (account) =>
            set({
                currentAccount: account,
            }),

        clearCurrentAccount: () =>
            set({
                currentAccount: null,
            }),

        addAccount: (account) =>
            set((state) => ({
                accounts: [
                    ...state.accounts,
                    account,
                ],
            })),

        removeAccount: (accountId) =>
            set((state) => ({
                accounts:
                    state.accounts.filter(
                        (account) =>
                            account.id !== accountId,
                    ),

                currentAccount:
                    state.currentAccount?.id ===
                    accountId
                        ? null
                        : state.currentAccount,
            })),

        updateAccountStatus: (
            accountId,
            status,
        ) =>
            set((state) => ({
                accounts:
                    state.accounts.map(
                        (account) =>
                            account.id === accountId
                                ? {
                                    ...account,
                                    status,
                                }
                                : account,
                    ),

                currentAccount:
                    state.currentAccount?.id ===
                    accountId
                        ? {
                            ...state.currentAccount,
                            status,
                        }
                        : state.currentAccount,
            })),
    }));