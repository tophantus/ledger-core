export const SUPPORTED_CURRENCIES = [
    "VND",
    "USD",
] as const;

export type SupportedCurrency =
    (typeof SUPPORTED_CURRENCIES)[number];