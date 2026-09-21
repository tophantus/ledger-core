import {z} from "zod";

export function createCardDetailsSchema(
    pinRequiredMessage: string,
) {
    return z.object({
        pin: z
            .string()
            .min(1, pinRequiredMessage),
    });
}

export type CardDetailsFormValues =
    z.infer<
        ReturnType<typeof createCardDetailsSchema>
    >;