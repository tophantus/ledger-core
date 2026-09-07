import {z} from "zod";

export const adminDepositSchema =
    z.object({
        amount: z
            .string()
            .trim()
            .min(1, "amountRequired")
            .regex(
                /^\d+(?:\.\d+)?$/,
                "invalidAmount",
            )
            .refine(
                (value) =>
                    Number(value) > 0,
                "amountMustBePositive",
            ),
    });

export type AdminDepositFormValues =
    z.infer<typeof adminDepositSchema>;