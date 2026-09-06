"use client";

import {accountApi} from "../api/account-api";
import type {CreateAccountRequest} from "../types/account";

export function useCreateAccount() {
    const createAccount = async (
        request: CreateAccountRequest,
    ) => {
        return accountApi.create(request);
    };

    return {
        createAccount,
    };
}