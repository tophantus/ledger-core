"use client";

import {accountApi} from "../api/account-api";
import {useAccountStore} from "../stores/account-store";
import type {
    AccountSummary,
    CreateAccountRequest,
} from "../types/account";

export function useCreateAccount() {
    const addAccount = useAccountStore(
        (state) => state.addAccount,
    );

    const createAccount = async (
        request: CreateAccountRequest,
    ) => {
        const response =
            await accountApi.create(request);

        if (!response.success) {
            return response;
        }

        const account = response.data;

        const summary: AccountSummary = {
            id: account.id,
            accountNo: account.accountNo,
            productId: account.productId,
            currency: account.currency,
            balance: account.balance,
            availableBalance: account.availableBalance,
            holdAmount: account.holdAmount,
            status: account.status,
        };

        addAccount(summary);

        return response;
    };

    return {
        createAccount,
    };
}