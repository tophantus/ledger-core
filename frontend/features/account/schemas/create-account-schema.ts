import {z} from "zod";

export const createAccountSchema = z.object({
    productId: z
        .string()
        .uuid("Invalid product"),

    currency: z
        .string()
        .trim()
        .length(
            3,
            "Currency must be 3 characters",
        )
        .toUpperCase(),
});

export type CreateAccountForm =
    z.infer<typeof createAccountSchema>;