"use client";

import {
    AlertCircle,
    Loader2,
} from "lucide-react";
import {useTranslations} from "next-intl";

import type {
    ReconciliationSummary as ReconciliationSummaryData,
} from "../types/admin-reconciliation";
import {ReconciliationSummaryCard} from "./reconciliation-summary-card";

interface ReconciliationSummaryProps {
    summary: ReconciliationSummaryData | null;
    loading: boolean;
    error: boolean;
}

export function ReconciliationSummary({
                                          summary,
                                          loading,
                                          error,
                                      }: ReconciliationSummaryProps) {
    const t = useTranslations(
        "admin.reconciliation",
    );

    if (loading) {
        return (
            <section className="space-y-4">
                <div>
                    <h2 className="text-lg font-semibold text-primary">
                        {t("summary.title")}
                    </h2>
                </div>

                <div className="flex items-center justify-center gap-2 rounded-lg border border-border bg-surface p-8 text-sm text-muted">
                    <Loader2 className="h-4 w-4 animate-spin" />
                    {t("summary.loading")}
                </div>
            </section>
        );
    }

    if (error || !summary) {
        return (
            <section className="space-y-4">
                <div>
                    <h2 className="text-lg font-semibold text-primary">
                        {t("summary.title")}
                    </h2>
                </div>

                <div className="flex items-center gap-3 rounded-lg border border-danger/30 bg-surface p-6 text-sm text-danger">
                    <AlertCircle className="h-5 w-5 shrink-0" />
                    {t("summary.loadError")}
                </div>
            </section>
        );
    }

    if (summary.runs.length === 0) {
        return (
            <section className="space-y-4">
                <div>
                    <h2 className="text-lg font-semibold text-primary">
                        {t("summary.title")}
                    </h2>
                </div>

                <div className="rounded-lg border border-border bg-surface p-8 text-center text-sm text-muted">
                    {t("summary.empty")}
                </div>
            </section>
        );
    }

    return (
        <section className="space-y-4">
            <div>
                <h2 className="text-lg font-semibold text-primary">
                    {t("summary.title")}
                </h2>

                <p className="mt-1 text-sm text-muted">
                    {t("summary.description")}
                </p>
            </div>

            <div className="grid gap-4 md:grid-cols-3">
                {summary.runs.map((run) => (
                    <ReconciliationSummaryCard
                        key={run.id}
                        run={run}
                    />
                ))}
            </div>
        </section>
    );
}