"use client";

import {create} from "zustand";

interface PendingVerification {
    userId: string;
    email: string;
}

interface AuthState {
    accessToken: string | null;
    pendingVerification: PendingVerification | null;

    setAccessToken: (accessToken: string) => void;
    clearAccessToken: () => void;

    setPendingVerification: (
        verification: PendingVerification,
    ) => void;

    clearPendingVerification: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
    accessToken: null,
    pendingVerification: null,

    setAccessToken: (accessToken) => {
        set({accessToken});
    },

    clearAccessToken: () => {
        set({accessToken: null});
    },

    setPendingVerification: (verification) => {
        set({
            pendingVerification: verification,
        });
    },

    clearPendingVerification: () => {
        set({
            pendingVerification: null,
        });
    },
}));