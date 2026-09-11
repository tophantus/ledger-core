import {z} from "zod";

export const WEBHOOK_EVENT_TYPES = [
    "ACCOUNT_BALANCE_CHANGED",
    "TRANSACTION_COMPLETED",
    "TRANSACTION_FAILED",
] as const;

export const updateWebhookSubscriptionsSchema =
    z.object({
        eventTypes: z
            .array(
                z.enum(
                    WEBHOOK_EVENT_TYPES,
                ),
            )
            .min(
                1,
                "At least one event is required",
            ),
    });

export type UpdateWebhookSubscriptionsForm =
    z.infer<
        typeof updateWebhookSubscriptionsSchema
    >;