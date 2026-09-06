"use client";

import {useTranslations} from "next-intl";

import {Logo} from "@/components/common/logo";
import {Button} from "@/components/ui/button";
import {useLogout} from "@/features/auth/hooks/use-logout";
import {useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

export function Topbar() {
    const t = useTranslations("dashboard");

    const {logout} = useLogout();

    const router = useRouter();

    const handleLogout = async () => {
        await logout();
        router.replace(ROUTES.AUTH.LOGIN);
    };

    return (
        <header className="border-b border-border bg-surface">
            <div className="mx-auto flex h-16 items-center justify-between px-2 md:px-6">
                <Logo size={36} />

                <Button
                    type="button"
                    variant="outline"
                    onClick={handleLogout}
                >
                    {t("logout")}
                </Button>
            </div>
        </header>
    );
}