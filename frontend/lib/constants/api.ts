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
    TRANSACTION: {
        BASE: "/api/v1/transactions",

        BY_ID: (transactionId: string) =>
            `/api/v1/transactions/${transactionId}`,

        ACCOUNT_TRANSACTIONS: (
            accountId: string,
        ) =>
            `/api/v1/transactions/accounts/${accountId}/transactions`,

        USER_TRANSACTIONS:
            "/api/v1/transactions",

        TRANSFER_INTENTS:
            "/api/v1/transactions/transfer-intents",

        CONFIRM_TRANSFER:
            "/api/v1/transactions/transfer-intents/confirm",

        WITHDRAW:
            "/api/v1/transactions/withdraw",
    },
    ADMIN: {
        ACCOUNT: {
            BASE: "/api/v1/admin/accounts",
            BY_ID: (accountId: string) =>
                `/api/v1/admin/accounts/${accountId}`,
        },
        TRANSACTION: {
            DEPOSIT: "/api/v1/admin/transactions/deposits",
        },
        RECONCILIATION: {
            SUMMARY: "/api/v1/admin/reconciliation/summary",
            EXCEPTIONS: "/api/v1/admin/reconciliation/exceptions",
        },
        BUSINESS_DAY: {
            CLOSE: "/api/v1/admin/business-days/close",
        },
    },
} as const;