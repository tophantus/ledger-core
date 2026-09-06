export const API_ENDPOINTS = {
    AUTH: {
        SIGN_UP: "/api/v1/auth/sign-up",
        VERIFY_EMAIL: "/api/v1/auth/verify-email",
        RESEND_VERIFICATION_CODE: "/api/v1/auth/verify-email/resend",
        LOGIN: "/api/v1/auth/login",
        REFRESH: "/api/v1/auth/refresh",
        LOGOUT: "/api/v1/auth/logout",
    },
    USER: {
        CURRENT_USER: "/api/v1/users/me",
    },
    ACCOUNT: {
        BASE: "/api/v1/accounts",
        BY_ID: (accountId: string) =>
            `/api/v1/accounts/${accountId}`,
        BY_NUMBER: (accountNo: string) =>
            `/api/v1/accounts/number/${accountNo}`,
        HOLDER: (accountNo: string) =>
            `/api/v1/accounts/number/${accountNo}/holder`,
        BLOCK: (accountId: string) =>
            `/api/v1/accounts/${accountId}/block`,
        ACTIVATE: (accountId: string) =>
            `/api/v1/accounts/${accountId}/activate`,
        CLOSE: (accountId: string) =>
            `/api/v1/accounts/${accountId}/close`,
    },
} as const;