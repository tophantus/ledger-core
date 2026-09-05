import {z} from "zod";

export const loginSchema = z.object({
    email: z.email(),
    password: z.string().min(8),
});

export const signUpSchema = z
    .object({
        fullName: z.string().min(1).max(100),
        email: z.email(),
        password: z.string().min(8),
        confirmPassword: z.string().min(8),
    })
    .refine(
        (data) => data.password === data.confirmPassword,
        {
            path: ["confirmPassword"],
            message: "Passwords do not match",
        },
    );

export const verifyEmailSchema = z.object({
    otp: z.string().length(6),
});

export const resendVerificationCodeSchema = z.object({
    email: z.email(),
});

export type LoginFormValues =
    z.infer<typeof loginSchema>;

export type SignUpFormValues =
    z.infer<typeof signUpSchema>;

export type VerifyEmailFormValues =
    z.infer<typeof verifyEmailSchema>;

export type ResendVerificationCodeFormValues =
    z.infer<typeof resendVerificationCodeSchema>;