"use client";

import {useCallback} from "react";

import {userApi} from "../api/user-api";
import {useUserStore} from "../stores/user-store";

export function useCurrentUser() {
    const setCurrentUser = useUserStore(
        (state) => state.setCurrentUser,
    );

    const getCurrentUser = useCallback(
        async () => {
            const response =
                await userApi.getCurrentUser();

            if (!response.success) {
                return response;
            }

            setCurrentUser(response.data);

            return response;
        },
        [setCurrentUser],
    );

    return {
        getCurrentUser,
    };
}