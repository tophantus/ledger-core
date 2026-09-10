import {z} from "zod";

export const withdrawalSchema = z.object({
    amount: z
        .string()
        .trim()
        .min(1, "amountRequired")
        .regex(
            /^\d+(?:\.\d+)?$/,
            "invalidAmount",
        ),
});

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