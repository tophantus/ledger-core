import axios from "axios";
import {useAuthStore} from "@/features/auth/stores/auth-store";

export const apiClient = axios.create({
    baseURL: "/api/backend",
    timeout: 10_000,
    withCredentials: true,
    headers: {
        "Content-Type": "application/json",
    },
});

apiClient.interceptors.request.use((config) => {
    const accessToken = useAuthStore.getState().accessToken;

    if (accessToken) {
        config.headers.Authorization = `Bearer ${accessToken}`;
    }

    return config;
});