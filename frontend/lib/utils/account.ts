// lib/utils/account.ts

import type {AccountStatus} from "@/features/account/types/account";

type Variant = "text" | "background";

export function getAccountStatusColor(
    status: AccountStatus,
    variant: Variant
): string {
    if (variant === "text") {
        switch (status) {
            case "ACTIVE":
                return "text-success";

            case "BLOCKED":
                return "text-warning";

            case "CLOSED":
                return "text-danger";
        }
    }

    switch (status) {
        case "ACTIVE":
            return "bg-success";

        case "BLOCKED":
            return "bg-warning";

        case "CLOSED":
            return "bg-danger";
    }

}