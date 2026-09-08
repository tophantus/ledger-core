"use client";

import type {
    ChangeEvent,
    FocusEvent,
    InputHTMLAttributes,
} from "react";
import {useLocale} from "next-intl";
import {useState} from "react";

import {
    formatMoney,
    formatMoneyInput,
    normalizeMoneyInput,
} from "@/lib/utils/currency";

interface MoneyInputProps
    extends Omit<
        InputHTMLAttributes<HTMLInputElement>,
        "value" | "onChange" | "type"
    > {
    value?: string;
    currency: string;
    onChange?: (value: string) => void;
    error?: string;
}

export function MoneyInput({
                               value = "",
                               currency,
                               onChange,
                               error,
                               className = "",
                               id,
                               onFocus,
                               onBlur,
                               ...props
                           }: MoneyInputProps) {
    const locale = useLocale();

    const [focused, setFocused] =
        useState(false);

    const handleChange = (
        event: ChangeEvent<HTMLInputElement>,
    ) => {
        const rawValue =
            normalizeMoneyInput(
                event.target.value,
                currency,
                locale,
            );

        onChange?.(rawValue);
    };

    const handleFocus = (
        event: FocusEvent<HTMLInputElement>,
    ) => {
        setFocused(true);
        onFocus?.(event);
    };

    const handleBlur = (
        event: FocusEvent<HTMLInputElement>,
    ) => {
        setFocused(false);
        onBlur?.(event);
    };

    const displayValue = focused
        ? formatMoneyInput(
            value,
            currency,
            locale,
        )
        : value
            ? formatMoney(
                value,
                currency,
                locale,
            )
            : "";

    return (
        <div className="space-y-2">
            <input
                {...props}
                id={id}
                type="text"
                inputMode="decimal"
                value={displayValue}
                onChange={handleChange}
                onFocus={handleFocus}
                onBlur={handleBlur}
                aria-invalid={Boolean(error)}
                aria-describedby={
                    error && id
                        ? `${id}-error`
                        : undefined
                }
                className={`
                    w-full rounded-md border
                    bg-surface px-3 py-2.5
                    text-text-primary
                    outline-none transition
                    placeholder:text-text-muted
                    disabled:cursor-not-allowed
                    disabled:opacity-60
                    ${
                    error
                        ? `
                                border-danger
                                focus:border-danger
                                focus:ring-2
                                focus:ring-danger/20
                            `
                        : `
                                border-border
                                focus:border-primary
                                focus:ring-2
                                focus:ring-primary/20
                            `
                }
                    ${className}
                `}
            />

            {error && (
                <p
                    id={
                        id
                            ? `${id}-error`
                            : undefined
                    }
                    className="text-sm text-danger"
                >
                    {error}
                </p>
            )}
        </div>
    );
}