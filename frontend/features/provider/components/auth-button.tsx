"use client";

import {ArrowLeft} from "lucide-react";

import {Link} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

export function AuthButton() {
    return (
        <Link
            href={ROUTES.AUTH.LOGIN}
            className="
                fixed top-6 left-6
                flex items-center gap-2
                rounded-full
                border border-border
                bg-surface
                px-4 py-3
                text-sm font-medium text-text-primary
                shadow-lg
                transition
                hover:bg-surface-subtle
                hover:shadow-xl
            "
        >
            <ArrowLeft className="h-4 w-4" />
        </Link>
    );
}