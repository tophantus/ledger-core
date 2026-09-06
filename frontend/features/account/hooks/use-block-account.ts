"use client";

import {accountApi} from "../api/account-api";
import {useAccountStore} from "../stores/account-store";

export function useBlockAccount() {
    const updateAccountStatus =
        useAccountStore(
            (state) => state.updateAccountStatus,
        );

    const blockAccount = async (
        accountId: string,
    ) => {
        const response =
            await accountApi.block(accountId);

        if (response.success) {
            updateAccountStatus(
                accountId,
                "BLOCKED",
            );
        }

        return response;
    };

    return {
        blockAccount,
    };
}