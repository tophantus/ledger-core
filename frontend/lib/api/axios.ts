import axios, {
    type AxiosRequestConfig,
} from "axios";

import {useAuthStore} from "@/features/auth/stores/auth-store";
import {API_ENDPOINTS} from "@/lib/constants/api";

interface RetryableRequestConfig extends AxiosRequestConfig {
    _retry?: boolean;
}

export const apiClient = axios.create({
    baseURL: "/api/backend",
    timeout: 10_000,
    withCredentials: true,
    headers: {
        "Content-Type": "application/json",
    },
    validateStatus: () => true,
});

const refreshClient = axios.create({
    baseURL: "/api/backend",
    timeout: 10_000,
    withCredentials: true,
    headers: {
        "Content-Type": "application/json",
    },
    validateStatus: () => true,
});

const logoutClient = axios.create({
    baseURL: "/api/backend",
    timeout: 10_000,
    withCredentials: true,
    headers: {
        "Content-Type": "application/json",
    },
    validateStatus: () => true,
});

let refreshPromise: Promise<string> | null = null;

apiClient.interceptors.request.use((config) => {
    const accessToken =
        useAuthStore.getState().accessToken;

    if (accessToken) {
        config.headers.Authorization =
            `Bearer ${accessToken}`;
    }

    return config;
});

async function refreshAccessToken(): Promise<string> {
    if (!refreshPromise) {
        refreshPromise = refreshClient
            .post(API_ENDPOINTS.AUTH.REFRESH)
            .then((response) => {
                if (
                    response.status < 200 ||
                    response.status >= 300
                ) {
                    throw new Error(
                        "Failed to refresh access token",
                    );
                }

                const accessToken =
                    response.data.data.accessToken;

                if (!accessToken) {
                    throw new Error(
                        "Access token is missing from refresh response",
                    );
                }

                useAuthStore
                    .getState()
                    .setAccessToken(accessToken);

                return accessToken;
            })
            .finally(() => {
                refreshPromise = null;
            });
    }

    return refreshPromise;
}

async function logoutAfterRefreshFailure(): Promise<void> {
    try {
        await logoutClient.post(
            API_ENDPOINTS.AUTH.LOGOUT,
        );
    } finally {
        useAuthStore
            .getState()
            .clearAccessToken();
    }
}

apiClient.interceptors.response.use(
    async (response) => {
        if (response.status !== 401) {
            return response;
        }

        const originalRequest =
            response.config as RetryableRequestConfig;

        if (originalRequest._retry) {
            return response;
        }

        originalRequest._retry = true;

        try {
            const accessToken =
                await refreshAccessToken();

            originalRequest.headers =
                originalRequest.headers ?? {};

            originalRequest.headers.Authorization =
                `Bearer ${accessToken}`;

            return apiClient(originalRequest);
        } catch {
            await logoutAfterRefreshFailure();

            return response;
        }
    },
);