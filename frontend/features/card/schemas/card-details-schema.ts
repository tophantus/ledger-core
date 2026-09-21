import {z} from "zod";

export function createCardDetailsSchema(
    pinRequiredMessage: string,
    pinInvalidMessage: string,
) {
    return z.object({
        pin: z
            .string()
            .min(1, pinRequiredMessage)
            .regex(/^\d{6}$/, pinInvalidMessage),
    });
}

export type CardDetailsFormValues =
    z.infer<
        ReturnType<typeof createCardDetailsSchema>
    >;