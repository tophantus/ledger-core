export function isZeroAmount(
    amount: string,
): boolean {
    return /^0+(?:\.0+)?$/.test(
        amount.trim(),
    );
}

export function isAmountLessThanOrEqual(
    amount: string,
    balance: string,
): boolean {
    const normalize = (value: string) => {
        const [integer = "0", decimal = ""] =
            value.trim().split(".");

        return {
            integer:
                integer.replace(/^0+/, "") ||
                "0",
            decimal: decimal.replace(/0+$/, ""),
        };
    };

    const normalizedAmount = normalize(amount);
    const normalizedBalance = normalize(balance);

    if (
        normalizedAmount.integer.length !==
        normalizedBalance.integer.length
    ) {
        return (
            normalizedAmount.integer.length <
            normalizedBalance.integer.length
        );
    }

    if (
        normalizedAmount.integer !==
        normalizedBalance.integer
    ) {
        return (
            normalizedAmount.integer <
            normalizedBalance.integer
        );
    }

    const maxDecimalLength = Math.max(
        normalizedAmount.decimal.length,
        normalizedBalance.decimal.length,
    );

    const amountDecimal =
        normalizedAmount.decimal.padEnd(
            maxDecimalLength,
            "0",
        );

    const balanceDecimal =
        normalizedBalance.decimal.padEnd(
            maxDecimalLength,
            "0",
        );

    return (
        amountDecimal <= balanceDecimal
    );
}