"use client";

import {
    useEffect,
    useState,
} from "react";
import {useTranslations} from "next-intl";

import {useAdminInterestRuns} from "@/features/admin/interest/hooks/use-admin-interest-runs";
import type {InterestRun} from "@/features/admin/interest/types/admin-interest";

import {InterestOverviewSummaryCard} from "./interest-overview-summary-card";
import {InterestOverviewRecentRuns} from "./interest-overview-recent-runs";
import {InterestOverviewQuickLinks} from "./interest-overview-quick-links";

const LATEST_RUNS_SIZE = 5;

export function InterestOverview() {
    const t = useTranslations(
        "admin.interest.overview",
    );

    const {getRuns} =
        useAdminInterestRuns();

    const [runs, setRuns] =
        useState<InterestRun[]>([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState(false);

    useEffect(() => {
        let cancelled = false;

        const loadRuns = async () => {
            try {
                const response = await getRuns({
                    page: 0,
                    size: LATEST_RUNS_SIZE,
                });

                if (cancelled) {
                    return;
                }

                if (!response.success) {
                    setError(true);
                    return;
                }

                setRuns(response.data.content);
                setError(false);
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

        void loadRuns();

        return () => {
            cancelled = true;
        };
    }, [getRuns]);

    const latestRun = runs[0];

    const latestAccrualRun =
        runs.find(
            (run) =>
                run.runType === "ACCRUAL",
        );

    const latestPostingRun =
        runs.find(
            (run) =>
                run.runType === "POSTING",
        );

    return (
        <section className="space-y-6">
            <div>
                <h1 className="
                    text-2xl
                    font-semibold
                    text-primary
                ">
                    {t("title")}
                </h1>

                <p className="
                    mt-1
                    text-sm
                    text-muted
                ">
                    {t("description")}
                </p>
            </div>

            <div className="
                grid
                gap-4
                md:grid-cols-3
            ">
                <InterestOverviewSummaryCard
                    title={t("latestRun")}
                    run={latestRun}
                    loading={loading}
                    icon="run"
                    t={t}
                />

                <InterestOverviewSummaryCard
                    title={t("latestAccrual")}
                    run={latestAccrualRun}
                    loading={loading}
                    icon="accrual"
                    t={t}
                />

                <InterestOverviewSummaryCard
                    title={t("latestPosting")}
                    run={latestPostingRun}
                    loading={loading}
                    icon="posting"
                    t={t}
                />
            </div>

            <InterestOverviewRecentRuns
                runs={runs}
                loading={loading}
                error={error}
                t={t}
            />

            <InterestOverviewQuickLinks
                t={t}
            />
        </section>
    );
}