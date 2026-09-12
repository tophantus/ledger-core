export const SUPPORTED_CURRENCIES = [
    "VND",
    "USD",
] as const;

export type Currency =
    (typeof SUPPORTED_CURRENCIES)[number];

export const WITHDRAWAL_DENOMINATIONS: Record<
    Currency,
    string
> = {
    VND: "50000",
    USD: "20",
};

export function getWithdrawalDenomination(
    currency: Currency,
): string {
    return WITHDRAWAL_DENOMINATIONS[currency];
}

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