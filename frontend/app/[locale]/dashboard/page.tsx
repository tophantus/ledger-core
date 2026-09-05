"use client";

import {useTranslations} from "next-intl";

import {useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";
import {useLogout} from "@/features/auth/hooks/use-logout";

export default function DashboardPage() {
    const t = useTranslations("dashboard");

    const router = useRouter();
    const {logout} = useLogout();

    const handleLogout = async () => {
        await logout();
        router.replace(ROUTES.AUTH.LOGIN);
    };

    return (
        <main className="min-h-screen bg-background">
            <header className="border-b border-border bg-surface">
                <div className="mx-auto flex h-16 max-w-7xl justify-end px-6">
                    <button
                        type="button"
                        onClick={handleLogout}
                        className="
                            my-auto inline-flex items-center
                            rounded-md
                            border border-border
                            bg-surface
                            px-4 py-2
                            text-sm font-medium text-text-secondary
                            transition-colors
                            hover:bg-surface-subtle
                            hover:text-text-primary
                            focus-visible:outline-none
                            focus-visible:ring-2
                            focus-visible:ring-ring
                            focus-visible:ring-offset-2
                        "
                    >
                        {t("logout")}
                    </button>
                </div>
            </header>

            <section className="mx-auto max-w-7xl px-6 py-8">
                <div className="rounded-lg border border-border bg-surface p-6 shadow-sm">
                    <h1 className="text-2xl font-semibold text-text-primary">
                        {t("title")}
                    </h1>

                    <p className="mt-2 text-sm text-text-muted">
                        {t("welcome")}
                    </p>
                </div>
            </section>
        </main>
    );
}