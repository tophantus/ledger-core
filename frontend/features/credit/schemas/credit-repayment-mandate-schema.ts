import {z} from "zod";

import {RepaymentType} from "../types/credit-repayment-mandate";

export function createCreditRepaymentMandateSchema(
    t: (key: string) => string,
) {
    return z.object({
        accountId: z
            .string()
            .min(1, t("repaymentMandate.validation.account.required")),

        repaymentType: z.enum(RepaymentType),
    });
}

export type CreditRepaymentMandateFormValues =
    z.infer<
        ReturnType<
            typeof createCreditRepaymentMandateSchema
        >
    >;