"use client";

import {authApi} from "../api/auth-api";
import {useAuthStore} from "../stores/auth-store";
import type {VerifyEmailRequest} from "../types/auth";

export function useVerifyEmail() {
    const setAccessToken = useAuthStore(
        (state) => state.setAccessToken,
    );

    const clearPendingVerification =
        useAuthStore(
            (state) => state.clearPendingVerification,
        );

    const verifyEmail = async (
        request: VerifyEmailRequest,
    ) => {
        const response =
            await authApi.verifyEmail(request);

        if (!response.success) {
            return response;
        }

        setAccessToken(
            response.data.accessToken,
        );

        clearPendingVerification();

        return response;
    };

    return {
        verifyEmail,
    };
}