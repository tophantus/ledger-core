"use client";

import {authApi} from "../api/auth-api";
import type {
    SendVerificationCodeRequest,
} from "../types/auth";

export function useSendVerificationCode() {
    const sendVerificationCode = async (
        request: SendVerificationCodeRequest,
    ) => {
        return authApi.sendVerificationCode(
            request,
        );
    };

    return {
        sendVerificationCode,
    };
}