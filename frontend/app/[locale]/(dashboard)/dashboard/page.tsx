"use client";

import {useTranslations} from "next-intl";

export default function DashboardPage() {
    const t = useTranslations("dashboard");

    return (
        <section>
            <div className="rounded-lg border border-border bg-surface p-6 shadow-sm">
                <h1 className="text-2xl font-semibold text-text-primary">
                    {t("title")}
                </h1>

                <p className="mt-2 text-sm text-text-muted">
                    {t("welcome")}
                </p>
            </div>
        </section>
    );
}