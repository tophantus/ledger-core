export const API_ENDPOINTS = {
    AUTH: {
        SIGN_UP: "/api/v1/auth/sign-up",
        VERIFY_EMAIL: "/api/v1/auth/verify-email",
        SEND_VERIFICATION_CODE: "/api/v1/auth/verify-email/send",
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

        TRANSFER_INTENTS:
            "/api/v1/transactions/transfer-intents",

        CONFIRM_TRANSFER:
            "/api/v1/transactions/transfer-intents/confirm"
    },
    WITHDRAWAL: {
        REQUESTS: "/api/v1/withdrawals/requests",
        CONFIRM: (requestId: string) =>
            `/api/v1/withdrawals/requests/${requestId}/confirm`,
        INTENTS: "/api/v1/withdrawals/intents",
        CANCEL_INTENT: (intentId: string) =>
            `/api/v1/withdrawals/intents/${intentId}/cancel`,
    },
    WEBHOOK: {
        BASE: "/api/v1/webhooks",

        ACCOUNT: (accountId: string) =>
            `/api/v1/accounts/${accountId}/webhooks`,

        BY_ID: (webhookId: string) =>
            `/api/v1/webhooks/${webhookId}`,

        SUBSCRIPTIONS: (webhookId: string) =>
            `/api/v1/webhooks/${webhookId}/subscriptions`,

        ROTATE_SECRET: (webhookId: string) =>
            `/api/v1/webhooks/${webhookId}/secret/rotate`,

        DELIVERIES: (webhookId: string) =>
            `/api/v1/webhooks/${webhookId}/deliveries`,
    },
    PRODUCT: {
        BASE: "/api/v1/products",
    },
    ATM: {
        EXECUTE_WITHDRAWAL: "/api/v1/withdrawals/execute",
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