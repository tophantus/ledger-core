"use client";

import {authApi} from "../api/auth-api";
import type {
    ResendVerificationCodeRequest,
} from "../types/auth";

export function useResendVerificationCode() {
    const resendVerificationCode = async (
        request: ResendVerificationCodeRequest,
    ) => {
        return authApi.resendVerificationCode(
            request,
        );
    };

    return {
        resendVerificationCode,
    };
}