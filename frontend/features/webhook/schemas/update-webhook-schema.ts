import {z} from "zod";

export const updateWebhookSchema =
    z.object({
        url: z.url(
                "Invalid URL",
            )
            .max(
                2048,
                "URL is too long",
            ),
    });

export type UpdateWebhookForm =
    z.infer<
        typeof updateWebhookSchema
    >;