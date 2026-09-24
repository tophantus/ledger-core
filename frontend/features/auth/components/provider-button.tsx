"use client";

import {Building2} from "lucide-react";

import {Link} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

export function ProviderButton() {
    return (
        <Link
            href={ROUTES.PROVIDER.DASHBOARD}
            className="
                fixed top-6 right-6
                flex items-center gap-2
                rounded-full bg-primary
                px-4 py-3
                text-sm font-medium text-primary-foreground
                shadow-lg
                transition
                hover:bg-primary/90
                hover:shadow-xl
            "
        >
            <Building2 className="h-4 w-4" />
        </Link>
    );
}