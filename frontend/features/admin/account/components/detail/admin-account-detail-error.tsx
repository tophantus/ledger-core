"use client";

import {ArrowLeft} from "lucide-react";
import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";
import {useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

export function AdminAccountDetailError() {
    const t = useTranslations(
        "admin.account.detail",
    );

    const router = useRouter();

    return (
        <div className="space-y-4">
            <Button
                type="button"
                variant="outline"
                onClick={() =>
                    router.push(
                        ROUTES.ADMIN.ACCOUNTS,
                    )
                }
            >
                <ArrowLeft className="mr-2 h-4 w-4" />
                {t("back")}
            </Button>

            <div
                className="
                    rounded-lg
                    border
                    border-border
                    bg-surface
                    p-6
                    text-sm
                    text-muted
                "
            >
                {t("loadError")}
            </div>
        </div>
    );
}