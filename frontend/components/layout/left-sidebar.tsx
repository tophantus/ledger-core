"use client";

import {useTranslations} from "next-intl";

import {Link, usePathname} from "@/i18n/routing";
import {NAVIGATION_ITEMS} from "@/lib/constants/navigation";

export function LeftSidebar() {
    const t = useTranslations("common");
    const pathname = usePathname();

    return (
        <aside className="hidden w-60 shrink-0 border-r border-border bg-surface md:block">
            <nav className="sticky top-0 h-[calc(100vh-4rem)] p-4">
                <div className="space-y-1">
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
                                    flex items-center gap-3
                                    rounded-md
                                    px-3 py-2.5
                                    text-sm font-medium
                                    transition
                                    ${
                                    isActive
                                        ? "bg-background-subtle text-text-primary"
                                        : "text-text-muted hover:bg-background-subtle hover:text-text-primary"
                                }
                                `}
                            >
                                <Icon className="h-5 w-5 shrink-0" />

                                <span>
                                    {t(item.labelKey)}
                                </span>
                            </Link>
                        );
                    })}
                </div>
            </nav>
        </aside>
    );
}