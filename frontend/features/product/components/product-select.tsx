"use client";

import {useEffect} from "react";
import {useTranslations} from "next-intl";

import {useProduct} from "../hooks/use-product";

interface ProductSelectProps {
    value: string;
    onChange: (value: string) => void;
    disabled?: boolean;
    error?: string;
}

export function ProductSelect({
                                  value,
                                  onChange,
                                  disabled = false,
                                  error,
                              }: ProductSelectProps) {
    const t = useTranslations("product");

    const {
        products,
        initialized,
        getActiveProducts,
    } = useProduct();

    useEffect(() => {
        if (initialized) {
            return;
        }

        void getActiveProducts();
    }, [
        initialized,
        getActiveProducts,
    ]);

    useEffect(() => {
        if (
            products.length > 0 &&
            !value
        ) {
            onChange(products[0].id);
        }
    }, [
        products,
        value,
        onChange,
    ]);

    if (!initialized || products.length === 0) {
        return null;
    }

    return (
        <div className="space-y-2">
            <label
                htmlFor="product"
                className="text-sm font-medium text-foreground"
            >
                {t("select.label")}
            </label>

            <select
                id="product"
                value={value}
                onChange={(event) =>
                    onChange(event.target.value)
                }
                disabled={disabled}
                className={`
                    h-10
                    w-full
                    rounded-md
                    border
                    bg-background
                    px-3
                    text-sm
                    text-foreground
                    outline-none
                    focus:ring-1
                    disabled:cursor-not-allowed
                    disabled:opacity-60
                    ${
                    error
                        ? "border-danger"
                        : "border-border"
                }
                `}
            >
                {products.map((product) => (
                    <option
                        key={product.id}
                        value={product.id}
                    >
                        {product.name}
                    </option>
                ))}
            </select>

            {error && (
                <p className="text-sm text-danger">
                    {error}
                </p>
            )}
        </div>
    );
}