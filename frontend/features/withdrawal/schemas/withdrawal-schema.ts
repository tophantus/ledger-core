import {z} from "zod";
import {
    SUPPORTED_CURRENCIES,
} from "@/lib/constants/currency";
import {isWithdrawalAmountMultiple} from "@/lib/utils/withdrawal";

export const withdrawalSchema = z
    .object({
        amount: z
            .string()
            .trim()
            .min(1, "amountRequired")
            .regex(
                /^\d+$/,
                "invalidAmount",
            ),

        currency: z.enum(
            SUPPORTED_CURRENCIES,
        ),
    })
    .refine(
        (values) =>
            isWithdrawalAmountMultiple(
                values.amount,
                values.currency,
            ),
        {
            path: ["amount"],
            message: "amountMultiple",
        },
    );

export type WithdrawalFormValues =
    z.infer<typeof withdrawalSchema>;

export const withdrawalOtpSchema = z.object({
    otp: z
        .string()
        .trim()
        .min(1, "otpRequired")
        .regex(
            /^\d{6}$/,
            "invalidOtp",
        ),
});

export type WithdrawalOtpForm =
    z.infer<typeof withdrawalOtpSchema>;