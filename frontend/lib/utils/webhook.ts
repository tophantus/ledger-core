import type {
    WebhookStatus,
} from "@/features/webhook/types/webhook";

type WebhookStatusColorVariant =
    | "text"
    | "background"
    | "text-background";

const WEBHOOK_STATUS_COLORS: Record<
    WebhookStatus,
    Record<
        WebhookStatusColorVariant,
        string
    >
> = {
    ACTIVE: {
        text: "text-success",
        background: "bg-success",
        "text-background":
            "text-success bg-success/10",
    },

    INACTIVE: {
        text: "text-muted",
        background: "bg-muted",
        "text-background":
            "text-muted bg-muted/10",
    },
};

export function getWebhookStatusColor(
    status: WebhookStatus,
    variant: WebhookStatusColorVariant,
): string {
    return WEBHOOK_STATUS_COLORS[
        status
        ][variant];
}