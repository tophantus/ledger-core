"use client";

import {authApi} from "../api/auth-api";
import {useAuthStore} from "../stores/auth-store";
import type {LoginRequest} from "../types/auth";

export function useLogin() {
    const setAccessToken = useAuthStore(
        (state) => state.setAccessToken,
    );

    const setPendingVerification = useAuthStore(
        (state) => state.setPendingVerification,
    );

    const login = async (
        request: LoginRequest,
    ) => {
        const response = await authApi.login(request);

        if (!response.success) {
            return response;
        }

        const {
            status,
            token,
            userId,
        } = response.data;

        if (status === "EMAIL_NOT_VERIFIED") {
            setPendingVerification({
                userId,
                email: request.email,
            });

            return response;
        }

        if (status === "AUTHENTICATED" && token) {
            setAccessToken(token.accessToken);

            return response;
        }

        throw new Error(
            "Invalid login response",
        );
    };

    return {
        login,
    };
}