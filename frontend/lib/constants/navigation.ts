import {
    ArrowDownToLine,
    ClipboardList, GitCompare,
    LayoutDashboard, Percent,
    ReceiptText, Users, Wallet, Webhook,
} from "lucide-react";

import {ROUTES} from "./routes";

export const NAVIGATION_ITEMS = [
    {
        href: ROUTES.DASHBOARD,
        labelKey: "navigation.dashboard",
        icon: LayoutDashboard,
    },
    {
        href: ROUTES.TRANSACTION.LIST,
        labelKey: "navigation.transactions",
        icon: ReceiptText,
    },
    {
        href: ROUTES.WITHDRAWALS,
        labelKey: "navigation.withdrawals",
        icon: ArrowDownToLine,
    },
    {
        href: ROUTES.WEBHOOKS.LIST,
        labelKey: "navigation.webhooks",
        icon: Webhook,
    },
] as const;

export const ADMIN_NAVIGATION_ITEMS = [
    {
        href: ROUTES.ADMIN.DASHBOARD,
        labelKey: "navigation.admin.dashboard",
        icon: LayoutDashboard,
    },
    {
        href: ROUTES.ADMIN.USERS,
        labelKey: "navigation.admin.users",
        icon: Users,
    },
    {
        href: ROUTES.ADMIN.ACCOUNTS,
        labelKey: "navigation.admin.accounts",
        icon: Wallet,
    },
    {
        href: ROUTES.ADMIN.TRANSACTIONS,
        labelKey: "navigation.admin.transactions",
        icon: ClipboardList,
    },
    {
        href: ROUTES.ADMIN.INTERESTS.OVERVIEW,
        labelKey: "navigation.admin.interests.overview",
        icon: Percent,
        children: [
            {
                href: ROUTES.ADMIN.INTERESTS.RUNS,
                labelKey:
                    "navigation.admin.interests.runs",
            },
            {
                href: ROUTES.ADMIN.INTERESTS.ACCRUALS,
                labelKey:
                    "navigation.admin.interests.accruals",
            },
            {
                href: ROUTES.ADMIN.INTERESTS.POSTINGS,
                labelKey:
                    "navigation.admin.interests.postings",
            },
        ],
    },
    {
        href: ROUTES.ADMIN.RECONCILIATION,
        labelKey: "navigation.admin.reconciliation",
        icon: GitCompare,
    },

] as const;