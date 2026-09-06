import {z} from "zod";

export const transferDestinationSchema = z.object({
    destinationAccountNo: z
        .string()
        .trim()
        .min(1, "Destination account is required")
        .max(30),
});

export const transferDetailsSchema = z.object({
    amount: z
        .string()
        .trim()
        .min(1, "Amount is required")
        .regex(
            /^\d+(?:\.\d+)?$/,
            "Invalid amount",
        )
        .refine(
            (value) =>
                !/^0+(?:\.0+)?$/.test(
                    value,
                ),
            {
                message:
                    "Amount must be greater than zero",
            },
        ),

    reference: z
        .string()
        .trim()
        .min(1, "Reference is required")
        .max(50),

    description: z
        .string()
        .max(500)
        .optional(),
});

export const transferOtpSchema = z.object({
    otp: z
        .string()
        .trim()
        .min(1, "OTP is required")
        .max(10),
});

export type TransferDestinationForm =
    z.infer<typeof transferDestinationSchema>;

export type TransferDetailsForm =
    z.infer<typeof transferDetailsSchema>;

export type TransferOtpForm =
    z.infer<typeof transferOtpSchema>;