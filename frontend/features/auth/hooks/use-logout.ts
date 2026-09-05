"use client";

import {authApi} from "../api/auth-api";
import {useAuthStore} from "../stores/auth-store";

export function useLogout() {
    const clearAccessToken =
        useAuthStore(
            (state) => state.clearAccessToken,
        );

    const logout = async () => {
        try {
            await authApi.logout();
        } finally {
            clearAccessToken();
        }
    };

    return {
        logout,
    };
}