import {z} from "zod";

import {CardType} from "@/features/card/types/card";

export function createCreateCardSchema(
    accountRequiredMessage: string,
    creditFacilityRequiredMessage: string,
    pinRequiredMessage: string,
    pinInvalidMessage: string,
) {
    return z
        .object({
            type: z.enum(CardType),

            accountId: z.string(),

            creditFacilityId: z.string(),

            pin: z
                .string()
                .min(
                    1,
                    pinRequiredMessage,
                )
                .regex(
                    /^\d{6}$/,
                    pinInvalidMessage,
                ),
        })
        .superRefine(
            (
                values,
                context,
            ) => {
                if (
                    values.type ===
                    CardType.DEBIT &&
                    !values.accountId
                ) {
                    context.addIssue({
                        code: "custom",
                        path: ["accountId"],
                        message:
                        accountRequiredMessage,
                    });
                }

                if (
                    values.type ===
                    CardType.CREDIT &&
                    !values.creditFacilityId
                ) {
                    context.addIssue({
                        code: "custom",
                        path: [
                            "creditFacilityId",
                        ],
                        message:
                        creditFacilityRequiredMessage,
                    });
                }
            },
        );
}

export type CreateCardFormValues =
    z.infer<
        ReturnType<
            typeof createCreateCardSchema
        >
    >;