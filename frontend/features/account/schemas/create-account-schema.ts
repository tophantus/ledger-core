import {z} from "zod";
import {SUPPORTED_CURRENCIES} from "@/lib/constants/currency";

export const createAccountSchema = z.object({
    productId: z.uuid("Invalid product"),

    currency: z.enum(
        SUPPORTED_CURRENCIES,
        {
            error: "Invalid currency",
        },
    ),
});

export type CreateAccountForm =
    z.infer<typeof createAccountSchema>;