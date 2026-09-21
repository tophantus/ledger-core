"use client";

import {
    Moon,
    Sun,
} from "lucide-react";
import {
    useEffect,
    useState,
} from "react";
import {useTheme} from "next-themes";

export function ThemeToggle() {
    const {
        theme,
        setTheme,
    } = useTheme();

    const [mounted, setMounted] =
        useState(false);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setMounted(true);
    }, []);

    if (!mounted) {
        return null;
    }

    const isDark = theme === "dark";

    const handleToggle = () => {
        setTheme(
            isDark
                ? "light"
                : "dark",
        );
    };

    return (
        <button
            type="button"
            onClick={handleToggle}
            aria-label={
                isDark
                    ? "Switch to light mode"
                    : "Switch to dark mode"
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
                {isDark ? (
                    <Moon className="h-4 w-4 text-muted" />
                ) : (
                    <Sun className="h-4 w-4 text-muted" />
                )}

                <span>
                    {isDark
                        ? "Dark mode"
                        : "Light mode"}
                </span>
            </span>

            <span className="text-xs font-medium text-muted">
                {isDark
                    ? "DARK"
                    : "LIGHT"}
            </span>
        </button>
    );
}