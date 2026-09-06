"use client";

import {accountApi} from "../api/account-api";
import {useAccountStore} from "../stores/account-store";

export function useCloseAccount() {
    const updateAccountStatus =
        useAccountStore(
            (state) => state.updateAccountStatus,
        );

    const closeAccount = async (
        accountId: string,
    ) => {
        const response =
            await accountApi.close(accountId);

        if (response.success) {
            updateAccountStatus(
                accountId,
                "CLOSED",
            );
        }

        return response;
    };

    return {
        closeAccount,
    };
}