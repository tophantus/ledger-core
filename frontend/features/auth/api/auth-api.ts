import {apiClient} from "@/lib/api/axios";
import {API_ENDPOINTS} from "@/lib/constants/api";
import type {ApiResponse} from "@/lib/api/types";

import type {
    LoginRequest,
    LoginResponse,
    LogoutResponse,
    ResendVerificationCodeRequest,
    SignUpRequest,
    SignUpResponse,
    TokenResponse,
    VerifyEmailRequest,
} from "../types/auth";

export const authApi = {
    signUp: async (
        request: SignUpRequest,
    ): Promise<ApiResponse<SignUpResponse>> => {
        const response = await apiClient.post<
            ApiResponse<SignUpResponse>
        >(
            API_ENDPOINTS.AUTH.SIGN_UP,
            request,
        );

        return response.data;
    },

    verifyEmail: async (
        request: VerifyEmailRequest,
    ): Promise<ApiResponse<TokenResponse>> => {
        const response = await apiClient.post<
            ApiResponse<TokenResponse>
        >(
            API_ENDPOINTS.AUTH.VERIFY_EMAIL,
            request,
        );

        return response.data;
    },

    resendVerificationCode: async (
        request: ResendVerificationCodeRequest,
    ): Promise<ApiResponse<void>> => {
        const response = await apiClient.post<
            ApiResponse<void>
        >(
            API_ENDPOINTS.AUTH.RESEND_VERIFICATION_CODE,
            request,
        );

        return response.data;
    },

    login: async (
        request: LoginRequest,
    ): Promise<ApiResponse<LoginResponse>> => {
        const response = await apiClient.post<
            ApiResponse<LoginResponse>
        >(
            API_ENDPOINTS.AUTH.LOGIN,
            request,
        );

        return response.data;
    },

    refresh: async (): Promise<ApiResponse<TokenResponse>> => {
        const response = await apiClient.post<
            ApiResponse<TokenResponse>
        >(
            API_ENDPOINTS.AUTH.REFRESH,
        );

        return response.data;
    },

    logout: async (): Promise<ApiResponse<LogoutResponse>> => {
        const response = await apiClient.post<
            ApiResponse<LogoutResponse>
        >(
            API_ENDPOINTS.AUTH.LOGOUT,
        );

        return response.data;
    },
};