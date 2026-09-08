import {z} from "zod";

export const adminDepositSchema =
    z.object({
        accountId: z.uuid("Invalid account"),
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