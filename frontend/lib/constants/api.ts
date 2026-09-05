export const API_ENDPOINTS = {
    AUTH: {
        SIGN_UP: "/api/v1/auth/sign-up",
        VERIFY_EMAIL: "/api/v1/auth/verify-email",
        RESEND_VERIFICATION_CODE: "/api/v1/auth/verify-email/resend",
        LOGIN: "/api/v1/auth/login",
        REFRESH: "/api/v1/auth/refresh",
        LOGOUT: "/api/v1/auth/logout",
    },
} as const;