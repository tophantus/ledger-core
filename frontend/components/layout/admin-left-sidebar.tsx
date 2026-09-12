"use client";

import {useState} from "react";
import {
    ChevronDown,
    ChevronRight,
} from "lucide-react";
import {useTranslations} from "next-intl";

import {Link, usePathname} from "@/i18n/routing";
import {ADMIN_NAVIGATION_ITEMS} from "@/lib/constants/navigation";

export function AdminLeftSidebar() {
    const t = useTranslations("common");
    const pathname = usePathname();

    const [expandedItems, setExpandedItems] =
        useState<Record<string, boolean>>({});

    const isPathActive = (
        href: string,
    ) => {
        return (
            pathname === href ||
            pathname.startsWith(
                `${href}/`,
            )
        );
    };

    const toggleExpanded = (
        href: string,
    ) => {
        setExpandedItems((current) => ({
            ...current,
            [href]: !current[href],
        }));
    };

    return (
        <aside className="
            hidden
            w-60
            shrink-0
            border-r
            border-border
            bg-surface
            md:block
        ">
            <nav className="
                sticky
                top-0
                h-[calc(100vh-4rem)]
                overflow-y-auto
                p-4
            ">
                <div className="space-y-1">
                    {ADMIN_NAVIGATION_ITEMS.map(
                        (item) => {
                            const Icon =
                                item.icon;

                            const hasChildren =
                                "children" in item &&
                                item.children.length > 0;

                            const isActive =
                                isPathActive(
                                    item.href,
                                );

                            const hasActiveChild =
                                hasChildren &&
                                item.children.some(
                                    (child) =>
                                        isPathActive(
                                            child.href,
                                        ),
                                );

                            const isExpanded =
                                expandedItems[
                                    item.href
                                    ] ??
                                hasActiveChild;

                            return (
                                <div
                                    key={item.href}
                                >
                                    <div className="
                                        flex
                                        items-center
                                        rounded-md
                                        transition
                                        hover:bg-background-subtle
                                    ">
                                        <Link
                                            href={
                                                item.href
                                            }
                                            className={`
                                                flex
                                                min-w-0
                                                flex-1
                                                items-center
                                                gap-3
                                                rounded-md
                                                px-3
                                                py-2.5
                                                text-sm
                                                font-medium
                                                transition
                                                ${
                                                isActive ||
                                                hasActiveChild
                                                    ? "text-primary"
                                                    : "text-muted hover:text-primary"
                                            }
                                            `}
                                        >
                                            <Icon className="
                                                h-5
                                                w-5
                                                shrink-0
                                            " />

                                            <span className="
                                                truncate
                                            ">
                                                {t(
                                                    item.labelKey,
                                                )}
                                            </span>
                                        </Link>

                                        {hasChildren && (
                                            <button
                                                type="button"
                                                aria-label={
                                                    isExpanded
                                                        ? `Collapse ${t(
                                                            item.labelKey,
                                                        )}`
                                                        : `Expand ${t(
                                                            item.labelKey,
                                                        )}`
                                                }
                                                aria-expanded={
                                                    isExpanded
                                                }
                                                onClick={() =>
                                                    toggleExpanded(
                                                        item.href,
                                                    )
                                                }
                                                className="
                                                    mr-1
                                                    flex
                                                    h-8
                                                    w-8
                                                    shrink-0
                                                    items-center
                                                    justify-center
                                                    rounded-md
                                                    text-muted
                                                    transition
                                                    hover:bg-background-subtle
                                                    hover:text-primary
                                                "
                                            >
                                                {isExpanded ? (
                                                    <ChevronDown className="
                                                        h-4
                                                        w-4
                                                    " />
                                                ) : (
                                                    <ChevronRight className="
                                                        h-4
                                                        w-4
                                                    " />
                                                )}
                                            </button>
                                        )}
                                    </div>

                                    {hasChildren &&
                                        isExpanded && (
                                            <div className="
                                                ml-5
                                                mt-1
                                                space-y-1
                                                border-l
                                                border-border
                                                pl-3
                                            ">
                                                {item.children.map(
                                                    (
                                                        child,
                                                    ) => {
                                                        const isChildActive =
                                                            isPathActive(
                                                                child.href,
                                                            );

                                                        return (
                                                            <Link
                                                                key={
                                                                    child.href
                                                                }
                                                                href={
                                                                    child.href
                                                                }
                                                                className={`
                                                                    block
                                                                    rounded-md
                                                                    px-3
                                                                    py-2
                                                                    text-sm
                                                                    transition
                                                                    ${
                                                                    isChildActive
                                                                        ? "bg-background-subtle font-medium text-primary"
                                                                        : "text-muted hover:bg-background-subtle hover:text-primary"
                                                                }
                                                                `}
                                                            >
                                                                {t(
                                                                    child.labelKey,
                                                                )}
                                                            </Link>
                                                        );
                                                    },
                                                )}
                                            </div>
                                        )}
                                </div>
                            );
                        },
                    )}
                </div>
            </nav>
        </aside>
    );
}