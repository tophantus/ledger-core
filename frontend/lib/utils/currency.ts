import {Currency} from "@/lib/constants/currency";

const CURRENCY_DECIMAL_DIGITS: Record<Currency, number> = {
    VND: 0,
    USD: 2,
};

export function getCurrencyDecimalDigits(
    currency: Currency,
): number {
    return (
        CURRENCY_DECIMAL_DIGITS[
            currency
            ] ?? 2
    );
}

export function getDecimalSeparator(
    locale: string,
): string {
    return new Intl.NumberFormat(locale)
        .formatToParts(1.1)
        .find(
            (part) => part.type === "decimal",
        )?.value ?? ".";
}

export function getThousandsSeparator(
    locale: string,
): string {
    return new Intl.NumberFormat(locale)
        .formatToParts(1000)
        .find(
            (part) => part.type === "group",
        )?.value ?? ",";
}

export function normalizeMoneyInput(
    value: string,
    currency: Currency,
    locale: string,
): string {
    if (!value) {
        return "";
    }

    const decimalDigits =
        getCurrencyDecimalDigits(currency);

    const decimalSeparator =
        getDecimalSeparator(locale);

    const groupSeparator =
        getThousandsSeparator(locale);

    const normalized = value
        .replace(/\s/g, "")
        .replace(new RegExp(`\\${groupSeparator}`, "g"), "");

    if (decimalDigits === 0) {
        return normalized.replace(/\D/g, "");
    }

    const escapedDecimal =
        decimalSeparator.replace(
            /[.*+?^${}()|[\]\\]/g,
            "\\$&",
        );

    const parts = normalized.split(
        new RegExp(escapedDecimal),
    );

    const integerPart =
        parts[0]?.replace(/\D/g, "") ?? "";

    const decimalPart =
        parts
            .slice(1)
            .join("")
            .replace(/\D/g, "")
            .slice(0, decimalDigits);

    if (!normalized.includes(decimalSeparator)) {
        return integerPart;
    }

    return `${integerPart || "0"}.${decimalPart}`;
}

export function formatMoneyInput(
    value: string | number,
    currency: Currency,
    locale: string,
): string {
    if (value === "") {
        return "";
    }

    const decimalDigits =
        getCurrencyDecimalDigits(currency);

    const decimalSeparator =
        getDecimalSeparator(locale);

    const normalized = String(value)
        .replace(/[^\d.]/g, "");

    if (!normalized) {
        return "";
    }

    const hasDecimal =
        normalized.includes(".");

    const [
        integerPart,
        decimalPart = "",
    ] = normalized.split(".");

    const formattedInteger =
        new Intl.NumberFormat(locale, {
            maximumFractionDigits: 0,
        }).format(
            Number(integerPart || "0"),
        );

    if (
        decimalDigits === 0 ||
        !hasDecimal
    ) {
        return formattedInteger;
    }

    return (
        `${formattedInteger}` +
        `${decimalSeparator}` +
        `${decimalPart.slice(
            0,
            decimalDigits,
        )}`
    );
}

export function formatMoney(
    amount: string | number,
    currency: Currency,
    locale: string,
): string {
    return new Intl.NumberFormat(locale, {
        style: "currency",
        currency,
    }).format(
        typeof amount === "string"
            ? Number(amount)
            : amount,
    );
}