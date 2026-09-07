"use client";

import {
    useEffect,
    useState,
} from "react";
import {useTranslations} from "next-intl";

import {CloseBusinessDayButton} from "@/features/admin/business-day/components/close-business-day-button";
import {ReconciliationSummary} from "@/features/admin/reconciliation/components/reconciliation-summary";
import {useAdminReconciliation} from "@/features/admin/reconciliation/hooks/use-admin-reconciliation";
import type {
    ReconciliationSummary as ReconciliationSummaryData,
} from "@/features/admin/reconciliation/types/admin-reconciliation";

export default function AdminDashboardPage() {
    const t = useTranslations(
        "admin.dashboard",
    );

    const {getSummary} =
        useAdminReconciliation();

    const [summary, setSummary] =
        useState<ReconciliationSummaryData | null>(
            null,
        );

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState(false);

    useEffect(() => {
        let cancelled = false;

        const loadSummary = async () => {
            try {
                const response =
                    await getSummary();

                if (cancelled) {
                    return;
                }

                if (!response.success) {
                    setError(true);
                    return;
                }

                setSummary(response.data);
            } catch {
                if (!cancelled) {
                    setError(true);
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        };

        void loadSummary();

        return () => {
            cancelled = true;
        };
    }, [getSummary]);

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-semibold text-primary">
                    {t("title")}
                </h1>

                <p className="mt-1 text-sm text-muted">
                    {t("description")}
                </p>
            </div>

            <section className="rounded-lg border border-border bg-surface p-6">
                <div className="flex flex-col gap-6 md:flex-row md:items-center md:justify-between">
                    <div>
                        <h2 className="text-lg font-semibold text-primary">
                            {t(
                                "businessDay.title",
                            )}
                        </h2>

                        <p className="mt-1 text-sm text-muted">
                            {t(
                                "businessDay.description",
                            )}
                        </p>
                    </div>

                    <CloseBusinessDayButton />
                </div>
            </section>

            <ReconciliationSummary
                summary={summary}
                loading={loading}
                error={error}
            />
        </div>
    );
}