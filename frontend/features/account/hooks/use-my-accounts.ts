"use client";

import {useCallback} from "react";

import {accountApi} from "../api/account-api";
import {useAccountStore} from "../stores/account-store";

export function useMyAccounts() {
    const setAccounts = useAccountStore(
        (state) => state.setAccounts,
    );

    const getMyAccounts = useCallback(
        async () => {
            const response =
                await accountApi.getMyAccounts();

            if (!response.success) {
                return response;
            }

            setAccounts(response.data);

            return response;
        },
        [setAccounts],
    );

    return {
        getMyAccounts,
    };
}