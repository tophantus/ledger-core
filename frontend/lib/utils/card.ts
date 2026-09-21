import type {CardStatus} from "@/features/card/types/card";

type Variant =
    | "text"
    | "background"
    | "text-background";

const CARD_STATUS_COLORS: Record<
    CardStatus,
    Record<Variant, string>
> = {
    ACTIVE: {
        text: "text-success",
        background: "bg-success",
        "text-background":
            "text-success bg-success/10",
    },
    INACTIVE: {
        text: "text-text-muted",
        background: "bg-text-muted",
        "text-background":
            "text-text-secondary bg-white/10",
    },
    BLOCKED: {
        text: "text-danger",
        background: "bg-danger",
        "text-background":
            "text-danger bg-danger/10",
    },
    CLOSED: {
        text: "text-text-muted",
        background: "bg-text-muted",
        "text-background":
            "text-text-secondary bg-white/10",
    },
};

export function getCardStatusColor(
    status: CardStatus,
    variant: Variant,
): string {
    return CARD_STATUS_COLORS[status][
        variant
        ];
}