export const SUPPORTED_CURRENCIES = [
    "VND",
    "USD",
] as const;

export type Currency =
    (typeof SUPPORTED_CURRENCIES)[number];

export function isCurrency(
    value: string,
): value is Currency {
    return SUPPORTED_CURRENCIES.includes(
        value as Currency,
    );
}

export function getCurrency(
    value: string,
): Currency | undefined {
    return SUPPORTED_CURRENCIES.find(
        (currency) => currency === value,
    );
}