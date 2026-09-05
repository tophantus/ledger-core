"use client";

import {authApi} from "../api/auth-api";
import {useAuthStore} from "../stores/auth-store";
import type {SignUpRequest} from "../types/auth";

export function useSignUp() {
    const setPendingVerification = useAuthStore(
        (state) => state.setPendingVerification,
    );

    const signUp = async (
        request: SignUpRequest,
    ) => {
        const response = await authApi.signUp(
            request,
        );

        if (!response.success) {
            return response;
        }

        const {
            userId,
            email,
            active,
        } = response.data;

        if (!active) {
            setPendingVerification({
                userId,
                email,
            });
        }

        return response;
    };

    return {
        signUp,
    };
}