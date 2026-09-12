"use client";

import Image from "next/image";
import {
    LogOut,
    Mail,
} from "lucide-react";
import {
    useEffect,
    useRef,
    useState,
} from "react";
import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";
import {useLogout} from "@/features/auth/hooks/use-logout";
import {useUserStore} from "@/features/user/stores/user-store";
import {useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

export function UserProfileMenu() {
    const t = useTranslations("dashboard");

    const {logout} = useLogout();
    const router = useRouter();

    const currentUser = useUserStore(
        (state) => state.currentUser,
    );

    const [isOpen, setIsOpen] =
        useState(false);

    const containerRef =
        useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClickOutside = (
            event: MouseEvent,
        ) => {
            if (
                containerRef.current &&
                !containerRef.current.contains(
                    event.target as Node,
                )
            ) {
                setIsOpen(false);
            }
        };

        document.addEventListener(
            "mousedown",
            handleClickOutside,
        );

        return () => {
            document.removeEventListener(
                "mousedown",
                handleClickOutside,
            );
        };
    }, []);

    const handleLogout = async () => {
        setIsOpen(false);

        await logout();

        router.replace(
            ROUTES.AUTH.LOGIN,
        );
    };

    if (!currentUser) {
        return null;
    }

    const fullName =
        currentUser.profile.fullName;

    const avatarFallback =
        fullName.trim().charAt(0).toUpperCase() ||
        "?";

    return (
        <div
            ref={containerRef}
            className="relative"
        >
            <button
                type="button"
                onClick={() =>
                    setIsOpen(
                        (open) => !open,
                    )
                }
                aria-label={fullName}
                aria-expanded={isOpen}
                className="
                    flex
                    h-10
                    w-10
                    items-center
                    justify-center
                    overflow-hidden
                    rounded-full
                    border
                    border-border
                    bg-background-subtle
                    text-sm
                    font-semibold
                    text-foreground
                    outline-none
                    transition
                    hover:bg-background
                    focus-visible:ring-2
                    focus-visible:ring-primary
                    focus-visible:ring-offset-2
                "
            >
                {currentUser.profile.avatarUrl ? (
                    <Image
                        src={
                            currentUser.profile
                                .avatarUrl
                        }
                        alt={fullName}
                        fill
                        sizes="40px"
                        className="object-cover"
                    />
                ) : (
                    avatarFallback
                )}
            </button>

            {isOpen && (
                <div
                    className="
                        absolute
                        right-0
                        top-12
                        z-50
                        w-80
                        overflow-hidden
                        rounded-lg
                        border
                        border-border
                        bg-surface
                        shadow-lg
                    "
                >
                    <div className="p-4">
                        <div className="flex items-center gap-3">
                            <div
                                className="
                                    relative
                                    flex
                                    h-12
                                    w-12
                                    shrink-0
                                    items-center
                                    justify-center
                                    overflow-hidden
                                    rounded-full
                                    bg-primary
                                    text-base
                                    font-semibold
                                    text-primary-foreground
                                "
                            >
                                {currentUser.profile
                                    .avatarUrl ? (
                                    <Image
                                        src={
                                            currentUser
                                                .profile
                                                .avatarUrl
                                        }
                                        alt={fullName}
                                        fill
                                        sizes="48px"
                                        className="object-cover"
                                    />
                                ) : (
                                    avatarFallback
                                )}
                            </div>

                            <div className="min-w-0">
                                <p className="truncate text-sm font-semibold text-foreground">
                                    {fullName}
                                </p>

                                <div className="mt-1 flex items-center gap-1.5">
                                    <Mail className="h-3.5 w-3.5 shrink-0 text-muted" />

                                    <p className="truncate text-xs text-muted">
                                        {
                                            currentUser.email
                                        }
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="border-t border-border p-2">
                        <Button
                            type="button"
                            variant="ghost"
                            className="w-full justify-start"
                            onClick={
                                handleLogout
                            }
                        >
                            <LogOut className="h-4 w-4" />

                            {t("logout")}
                        </Button>
                    </div>
                </div>
            )}
        </div>
    );
}
