"use client";

import {accountApi} from "../api/account-api";
import {useAccountStore} from "../stores/account-store";
import {AccountStatus} from "@/features/account/types/account";

export function useActivateAccount() {
    const updateAccountStatus =
        useAccountStore(
            (state) => state.updateAccountStatus,
        );

    const activateAccount = async (
        accountId: string,
    ) => {
        const response =
            await accountApi.activate(accountId);

        if (response.success) {
            updateAccountStatus(
                accountId,
                AccountStatus.ACTIVE,
            );
        }

        return response;
    };

    return {
        activateAccount,
    };
}