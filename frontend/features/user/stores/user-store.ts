"use client";

import {create} from "zustand";

import type {CurrentUser} from "../types/user";

interface UserState {
    currentUser: CurrentUser | null;

    setCurrentUser: (
        user: CurrentUser,
    ) => void;

    clearCurrentUser: () => void;
}

export const useUserStore = create<UserState>(
    (set) => ({
        currentUser: null,

        setCurrentUser: (currentUser) =>
            set({
                currentUser,
            }),

        clearCurrentUser: () =>
            set({
                currentUser: null,
            }),
    }),
);