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
        LIST: "/webhooks",
        DETAILS: (webhookId: string) =>
            `/webhooks/${webhookId}`,
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
        INTERESTS: {
            OVERVIEW: "/admin/interests",
            RUNS: "/admin/interests/runs",
            ACCRUALS: "/admin/interests/accruals",
            POSTINGS: "/admin/interests/postings",
        },
    },
} as const;