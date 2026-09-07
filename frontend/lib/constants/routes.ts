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