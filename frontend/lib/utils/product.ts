type ProductColorType =
    | "text"
    | "background";

const PRODUCT_COLORS: Record<
    string,
    Record<ProductColorType, string>
> = {
    SAVINGS: {
        text: "text-blue-600",
        background: "bg-blue-50",
    },
    CURRENT: {
        text: "text-emerald-600",
        background: "bg-emerald-50",
    },
};

const DEFAULT_PRODUCT_COLOR = {
    text: "text-text-secondary",
    background: "bg-surface-subtle",
};

export function getProductColor(
    code: string,
    type: ProductColorType,
): string {
    return (
        PRODUCT_COLORS[code]?.[type]
        ?? DEFAULT_PRODUCT_COLOR[type]
    );
}