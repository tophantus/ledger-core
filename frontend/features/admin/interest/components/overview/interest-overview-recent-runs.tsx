import {ArrowRight} from "lucide-react";
import Link from "next/link";

import type {InterestRun} from "@/features/admin/interest/types/admin-interest";
import {ROUTES} from "@/lib/constants/routes";

import {InterestOverviewRunRow} from "./interest-overview-run-row";
import {InterestOverviewSkeleton} from "./interest-overview-skeleton";

interface InterestOverviewRecentRunsProps {
    runs: InterestRun[];
    loading: boolean;
    error: boolean;
    t: ReturnType<typeof import("next-intl").useTranslations>;
}

export function InterestOverviewRecentRuns({
                                               runs,
                                               loading,
                                               error,
                                               t,
                                           }: InterestOverviewRecentRunsProps) {
    return (
        <section className="
            overflow-hidden
            rounded-lg
            border
            border-border
            bg-surface
        ">
            <div className="
                flex
                items-center
                justify-between
                border-b
                border-border
                px-4
                py-4
            ">
                <div>
                    <h2 className="
                        text-base
                        font-semibold
                        text-foreground
                    ">
                        {t("recentRuns")}
                    </h2>

                    <p className="
                        mt-1
                        text-sm
                        text-muted
                    ">
                        {t("recentRunsDescription")}
                    </p>
                </div>

                <Link
                    href={ROUTES.ADMIN.INTERESTS.RUNS}
                    className="
                        inline-flex
                        items-center
                        gap-1
                        text-sm
                        font-medium
                        text-primary
                        hover:underline
                    "
                >
                    {t("viewAll")}
                    <ArrowRight className="h-4 w-4" />
                </Link>
            </div>

            {loading ? (
                <InterestOverviewSkeleton />
            ) : error ? (
                <div className="p-6">
                    <p className="
                        text-sm
                        text-danger
                    ">
                        {t("loadError")}
                    </p>
                </div>
            ) : runs.length === 0 ? (
                <div className="p-6">
                    <p className="
                        text-sm
                        text-muted
                    ">
                        {t("noRuns")}
                    </p>
                </div>
            ) : (
                <div className="overflow-x-auto">
                    <table className="
                        w-full
                        text-sm
                    ">
                        <thead>
                        <tr className="
                            border-b
                            border-border
                            text-left
                            text-muted
                        ">
                            <th className="px-4 py-3 font-medium">
                                {t("columns.businessDate")}
                            </th>

                            <th className="px-4 py-3 font-medium">
                                {t("columns.type")}
                            </th>

                            <th className="px-4 py-3 font-medium">
                                {t("columns.status")}
                            </th>

                            <th className="px-4 py-3 font-medium">
                                {t("columns.processed")}
                            </th>

                            <th className="px-4 py-3 font-medium">
                                {t("columns.startedAt")}
                            </th>

                            <th className="px-4 py-3 font-medium">
                                {t("columns.completedAt")}
                            </th>
                        </tr>
                        </thead>

                        <tbody>
                        {runs.map((run) => (
                            <InterestOverviewRunRow
                                key={run.id}
                                run={run}
                                t={t}
                            />
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </section>
    );
}