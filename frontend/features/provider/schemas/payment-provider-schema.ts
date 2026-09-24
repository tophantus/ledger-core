import {z} from "zod";

import {ProviderType} from "../types/payment-provider";

export function createRegisterPaymentProviderSchema(
    t: (key: string) => string,
) {
    return z.object({
        code: z
            .string()
            .trim()
            .min(
                1,
                t("validation.code.required"),
            )
            .max(
                50,
                t("validation.code.maxLength"),
            )
            .regex(
                /^[A-Z0-9_]+$/,
                t("validation.code.pattern"),
            ),

        name: z
            .string()
            .trim()
            .min(
                1,
                t("validation.name.required"),
            )
            .max(
                100,
                t("validation.name.maxLength"),
            ),

        type: z.enum(ProviderType),
    });
}

export type RegisterPaymentProviderFormValues =
    z.infer<
        ReturnType<
            typeof createRegisterPaymentProviderSchema
        >
    >;