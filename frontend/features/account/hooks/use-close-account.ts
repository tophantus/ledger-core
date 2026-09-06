"use client";

import {accountApi} from "../api/account-api";
import {useAccountStore} from "../stores/account-store";

export function useCloseAccount() {
    const removeAccount = useAccountStore(
        (state) => state.removeAccount,
    );

    const closeAccount = async (
        accountId: string,
    ) => {
        const response =
            await accountApi.close(accountId);

        if (response.success) {
            removeAccount(accountId);
        }

        return response;
    };

    return {
        closeAccount,
    };
}