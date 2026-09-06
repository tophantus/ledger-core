"use client";

import {useTranslations} from "next-intl";

import {Link, usePathname} from "@/i18n/routing";
import {NAVIGATION_ITEMS} from "@/lib/constants/navigation";

export function BottomBar() {
    const t = useTranslations("common");
    const pathname = usePathname();

    return (
        <nav
            className="
                fixed
                inset-x-0
                bottom-0
                z-50
                border-t
                border-border
                bg-surface
                md:hidden
            "
        >
            <div className="mx-auto flex max-w-md items-center justify-around">
                {NAVIGATION_ITEMS.map((item) => {
                    const Icon = item.icon;

                    const isActive =
                        pathname === item.href ||
                            pathname.startsWith(
                                `${item.href}/`);

                    return (
                        <Link
                            key={item.href}
                            href={item.href}
                            className={`
                                flex
                                min-w-20
                                flex-col
                                items-center
                                gap-1
                                px-4
                                py-2.5
                                text-xs
                                font-medium
                                transition
                                ${
                                isActive
                                    ? "text-text-primary"
                                    : "text-text-muted"
                            }
                            `}
                        >
                            <Icon className="h-5 w-5" />

                            <span>
                                {t(item.labelKey)}
                            </span>
                        </Link>
                    );
                })}
            </div>
        </nav>
    );
}