export const ROUTES = {
    AUTH: {
        LOGIN: "/login",
        REGISTER: "/register",
        VERIFY_EMAIL: "/verify-email",
    },

    DASHBOARD: "/dashboard",

    ACCOUNT: {
        DETAIL: (accountId: string) =>
            `/accounts/${accountId}`,
    },

    TRANSACTION: {
        LIST: "/transactions",
        TRANSFER: "/transfer",
        WITHDRAW: "/withdraw",
        DETAIL: (transactionId: string) =>
            `/transactions/${transactionId}`,
    },
} as const;