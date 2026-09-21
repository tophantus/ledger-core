import {z} from "zod";

import {WebhookEventType} from "../types/webhook";

export const WEBHOOK_EVENT_TYPES = Object.values(
    WebhookEventType
)

export const registerWebhookSchema = z.object({
    accountId: z.uuid("Account is required"),

    url: z.url("Invalid URL")
        .max(2048, "URL is too long"),

    eventTypes: z
        .array(z.enum(WebhookEventType))
        .min(1, "At least one event is required"),
});

export type RegisterWebhookForm =
    z.infer<typeof registerWebhookSchema>;