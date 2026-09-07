"use client";

import {
    CheckCircle2,
    Clock,
    Loader2,
} from "lucide-react";
import {useTranslations} from "next-intl";

import type {
    ReconciliationRunSummary,
} from "../types/admin-reconciliation";

interface ReconciliationSummaryCardProps {
    run: ReconciliationRunSummary;
}

export function ReconciliationSummaryCard({
                                              run,
                                          }: ReconciliationSummaryCardProps) {
    const t = useTranslations(
        "admin.reconciliation",
    );

    const StatusIcon = {
        PENDING: Clock,
        RUNNING: Loader2,
        COMPLETED: CheckCircle2,
    }[run.status];

    return (
        <div className="rounded-lg border border-border bg-surface p-5">
            <div className="flex items-start justify-between gap-4">
                <div>
                    <p className="text-sm font-medium text-muted">
                        {t(`types.${run.type}`)}
                    </p>

                    <p className="mt-2 text-2xl font-semibold text-primary">
                        {run.processedCount.toLocaleString()}
                    </p>

                    <p className="mt-1 text-xs text-muted">
                        {t("summary.processed")}
                    </p>
                </div>

                <StatusIcon
                    className={
                        run.status === "RUNNING"
                            ? "h-5 w-5 animate-spin"
                            : "h-5 w-5"
                    }
                />
            </div>

            <div className="mt-4 flex items-center gap-2 text-sm">
                <span className="text-muted">
                    {t("summary.status")}
                </span>

                <span className="font-medium text-primary">
                    {t(
                        `statuses.${run.status}`,
                    )}
                </span>
            </div>
        </div>
    );
}