"use client";

import {Plus} from "lucide-react";
import {useTranslations} from "next-intl";

interface CreateAccountCardProps {
    onClick: () => void;
}

export function CreateAccountCard({
                                      onClick,
                                  }: CreateAccountCardProps) {
    const t = useTranslations("account");

    return (
        <button
            type="button"
            onClick={onClick}
            className="
                flex
                min-h-48
                w-full
                flex-col
                items-center
                justify-center
                rounded-lg
                border
                border-dashed
                border-border
                bg-surface
                p-5
                text-center
                transition
                hover:border-primary
                hover:bg-background-subtle
            "
        >
            <div
                className="
                    flex
                    h-10
                    w-10
                    items-center
                    justify-center
                    rounded-full
                    bg-secondary
                    text-secondary-foreground
                "
            >
                <Plus className="h-5 w-5" />
            </div>

            <p className="mt-3 text-sm font-medium text-foreground">
                {t("create.title")}
            </p>

            <p className="mt-1 text-xs text-text-muted">
                {t("create.description")}
            </p>
        </button>
    );
}