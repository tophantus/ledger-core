"use client";

import {create} from "zustand";

import type {AccountSummary} from "../types/account";

interface AccountState {
    accounts: AccountSummary[];

    setAccounts: (
        accounts: AccountSummary[],
    ) => void;

    clearAccounts: () => void;

    updateAccountStatus: (
        accountId: string,
        status: AccountSummary["status"],
    ) => void;
}

export const useAccountStore =
    create<AccountState>((set) => ({
        accounts: [],

        setAccounts: (accounts) =>
            set({
                accounts,
            }),

        clearAccounts: () =>
            set({
                accounts: [],
            }),

        updateAccountStatus: (
            accountId,
            status,
        ) =>
            set((state) => ({
                accounts: state.accounts.map(
                    (account) =>
                        account.id === accountId
                            ? {
                                ...account,
                                status,
                            }
                            : account,
                ),
            })),
    }));