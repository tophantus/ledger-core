import type {
    WithdrawalIntentStatus,
} from "@/features/withdrawal/types/withdrawal";

type Variant =
    | "text"
    | "background"
    | "text-background";

const STATUS_COLORS: Record<
    WithdrawalIntentStatus,
    Record<Variant, string>
> = {
    READY: {
        text: "text-primary",
        background: "bg-primary",
        "text-background":
            "text-primary bg-primary/10",
    },
    COMPLETED: {
        text: "text-success",
        background: "bg-success",
        "text-background":
            "text-success bg-success/10",
    },
    EXPIRED: {
        text: "text-warning",
        background: "bg-warning",
        "text-background":
            "text-warning bg-warning/10",
    },
    CANCELLED: {
        text: "text-danger",
        background: "bg-danger",
        "text-background":
            "text-danger bg-danger/10",
    },
};

export function getWithdrawalStatusColor(
    status: WithdrawalIntentStatus,
    variant: Variant,
): string {
    return STATUS_COLORS[status][variant];
}