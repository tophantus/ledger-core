"use client";

import {Globe} from "lucide-react";
import {useLocale} from "next-intl";

import {usePathname, useRouter} from "@/i18n/routing";

export function LocaleToggle() {
    const locale = useLocale();
    const router = useRouter();
    const pathname = usePathname();

    const nextLocale =
        locale === "vi" ? "en" : "vi";

    const handleToggle = () => {
        router.replace(pathname, {
            locale: nextLocale,
        });
    };

    return (
        <button
            type="button"
            onClick={handleToggle}
            aria-label={
                locale === "vi"
                    ? "Switch to English"
                    : "Chuyển sang tiếng Việt"
            }
            className="
                flex
                w-full
                items-center
                justify-between
                rounded-md
                px-3
                py-2
                text-sm
                text-foreground
                transition
                hover:bg-background
            "
        >
            <span className="flex items-center gap-2">
                <Globe className="h-4 w-4 text-muted" />

                <span>
                    {locale === "vi"
                        ? "Ngôn ngữ"
                        : "Language"}
                </span>
            </span>

            <span className="text-xs font-medium text-muted">
                {nextLocale.toUpperCase()}
            </span>
        </button>
    );
}