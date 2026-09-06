import {
    LayoutDashboard,
    ReceiptText,
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
] as const;