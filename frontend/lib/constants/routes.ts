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
    TRANSFER: "/transfer",
    WITHDRAWAL: "/withdrawal",
    WITHDRAWALS: "/withdrawals",
    WEBHOOKS: {
        LIST: "/webhooks"
    },
    TRANSACTION: {
        LIST: "/transactions",
        DETAIL: (transactionId: string) =>
            `/transactions/${transactionId}`,
    },

    ADMIN: {
        DASHBOARD: "/admin/dashboard",
        USERS: "/admin/users",
        ACCOUNTS: "/admin/accounts",
        ACCOUNT_DETAIL: (accountId: string) =>
            `/admin/accounts/${accountId}`,
        TRANSACTIONS: "/admin/transactions",
        RECONCILIATION: "/admin/reconciliation",
    },
} as const;