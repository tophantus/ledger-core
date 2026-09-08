"use client";

import {useTranslations} from "next-intl";

import {useProductStore} from "../store/product-store";
import {getProductColor} from "@/lib/utils/product";

interface ProductBadgeProps {
    productId: string;
}

export function ProductBadge({
                                 productId,
                             }: ProductBadgeProps) {
    const t = useTranslations("product");

    const product = useProductStore(
        (state) =>
            state.productMap.get(productId),
    );

    if (!product) {
        return null;
    }

    return (
        <div
            className={`
                inline-flex
                shrink-0
                rounded-full
                px-2.5
                py-1
                text-xs
                font-medium
                ${getProductColor(
                product.code,
                "text",
            )}
                ${getProductColor(
                product.code,
                "background",
            )}
            `}
        >
            {t(`names.${product.code}`)}
        </div>
    );
}