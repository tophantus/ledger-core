import type {AccountStatus} from "@/features/account/types/account";

type Variant =
    | "text"
    | "background"
    | "text-background";

const STATUS_COLORS: Record<
    AccountStatus,
    Record<Variant, string>
> = {
    ACTIVE: {
        text: "text-success",
        background: "bg-success",
        "text-background":
            "text-success bg-success/10",
    },
    BLOCKED: {
        text: "text-warning",
        background: "bg-warning",
        "text-background":
            "text-warning bg-warning/10",
    },
    CLOSED: {
        text: "text-danger",
        background: "bg-danger",
        "text-background":
            "text-danger bg-danger/10",
    },
};

export function getAccountStatusColor(
    status: AccountStatus,
    variant: Variant,
): string {
    return STATUS_COLORS[status][variant];
}