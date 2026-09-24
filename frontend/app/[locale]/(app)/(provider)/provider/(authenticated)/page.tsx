"use client";

import {useTranslations} from "next-intl";

import {ProviderAccountList} from "@/features/provider/account/components/provider-account-list";

export default function ProviderDashboardPage() {
    const t = useTranslations("provider.dashboard");

    const providerName =
        process.env.NEXT_PUBLIC_PROVIDER_NAME ?? "";

    return (
        <section className="space-y-6">
            <div>
                <h1 className="text-2xl font-semibold text-text-primary">
                    {t("title")}
                </h1>

                <p className="mt-1 text-sm text-text-muted">
                    {t("welcome", {
                        name: providerName,
                    })}
                </p>
            </div>

            <ProviderAccountList />
        </section>
    );
}