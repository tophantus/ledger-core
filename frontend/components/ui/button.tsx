import type {
    ButtonHTMLAttributes,
    ReactNode,
} from "react";
import {TailSpin} from "react-loader-spinner";

type ButtonVariant =
    | "primary"
    | "secondary"
    | "danger"
    | "outline"
    | "ghost";

interface ButtonProps
    extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: ButtonVariant;
    loading?: boolean;
    children: ReactNode;
}

const VARIANT_CLASSES: Record<
    ButtonVariant,
    string
> = {
    primary: `
        bg-primary
        text-primary-foreground
        hover:bg-primary-hover
    `,
    secondary: `
        bg-secondary
        text-secondary-foreground
        hover:bg-secondary-hover
    `,
    danger: `
        bg-danger
        text-danger-foreground
        hover:bg-danger-hover
    `,
    outline: `
        border
        border-border
        bg-surface
        text-text-primary
        hover:bg-surface-subtle
    `,
    ghost: `
        bg-transparent
        text-text-secondary
        hover:bg-surface-subtle
        hover:text-text-primary
    `,
};

export function Button({
                           variant = "primary",
                           loading = false,
                           disabled,
                           children,
                           className = "",
                           ...props
                       }: ButtonProps) {
    return (
        <button
            {...props}
            disabled={disabled || loading}
            className={`
                inline-flex
                items-center
                justify-center
                gap-2
                rounded-md
                px-4
                py-2.5
                text-sm
                font-medium
                transition-colors
                focus-visible:outline-none
                focus-visible:ring-2
                focus-visible:ring-ring
                focus-visible:ring-offset-2
                disabled:cursor-not-allowed
                disabled:opacity-60
                ${VARIANT_CLASSES[variant]}
                ${className}
            `}
        >
            {loading ? (
                <TailSpin
                    visible
                    height="18"
                    width="18"
                    color="currentColor"
                    ariaLabel="loading"
                />
            ) : (
                children
            )}
        </button>
    );
}