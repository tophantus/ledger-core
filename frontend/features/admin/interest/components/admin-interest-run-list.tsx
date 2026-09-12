"use client";

import {useLocale, useTranslations} from "next-intl";

import type {
    InterestRun,
} from "../types/admin-interest";

interface AdminInterestRunListProps {
    runs: InterestRun[];
    loading: boolean;
}

function formatDate(
    value: string,
    locale: string,
) {
    return new Intl.DateTimeFormat(
        locale,
        {
            year: "numeric",
            month: "short",
            day: "numeric",
        },
    ).format(
        new Date(
            `${value}T00:00:00`,
        ),
    );
}

function formatDateTime(
    value: string | null,
    locale: string,
) {
    if (!value) {
        return "-";
    }

    return new Intl.DateTimeFormat(
        locale,
        {
            year: "numeric",
            month: "short",
            day: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        },
    ).format(new Date(value));
}

export function AdminInterestRunList({
                                         runs,
                                         loading,
                                     }: AdminInterestRunListProps) {
    const t = useTranslations(
        "admin.interest.runs",
    );
    const locale = useLocale();

    if (loading) {
        return (
            <div className="rounded-lg border border-border bg-surface p-6">
                {t("loading")}
            </div>
        );
    }

    if (runs.length === 0) {
        return (
            <div className="rounded-lg border border-border bg-surface p-6 text-center text-muted">
                {t("empty")}
            </div>
        );
    }

    return (
        <div className="overflow-x-auto rounded-lg border border-border bg-surface">
            <table className="w-full text-sm">
                <thead className="border-b border-border">
                <tr className="text-left text-muted">
                    <th className="whitespace-nowrap px-4 py-3">
                        {t("table.businessDate")}
                    </th>

                    <th className="whitespace-nowrap px-4 py-3">
                        {t("table.runType")}
                    </th>

                    <th className="whitespace-nowrap px-4 py-3">
                        {t("table.status")}
                    </th>

                    <th className="whitespace-nowrap px-4 py-3">
                        {t("table.processedCount")}
                    </th>

                    <th className="whitespace-nowrap px-4 py-3">
                        {t("table.startedAt")}
                    </th>

                    <th className="whitespace-nowrap px-4 py-3">
                        {t("table.completedAt")}
                    </th>

                    <th className="whitespace-nowrap px-4 py-3">
                        {t("table.createdAt")}
                    </th>
                </tr>
                </thead>

                <tbody>
                {runs.map((run) => (
                    <tr
                        key={run.id}
                        className="
                            border-b
                            border-border
                            last:border-0
                        "
                    >
                        <td className="whitespace-nowrap px-4 py-3 font-medium text-primary">
                            {formatDate(
                                run.businessDate,
                                locale,
                            )}
                        </td>

                        <td className="px-4 py-3">
                            <span className="
                                inline-flex
                                rounded-full
                                bg-primary/10
                                px-2.5
                                py-1
                                text-xs
                                font-medium
                                text-primary
                            ">
                                {t(
                                    `runTypes.${run.runType}`,
                                )}
                            </span>
                        </td>

                        <td className="px-4 py-3">
                            <span
                                className={`
                                    inline-flex
                                    rounded-full
                                    px-2.5
                                    py-1
                                    text-xs
                                    font-medium
                                    ${
                                    run.status ===
                                    "COMPLETED"
                                        ? "bg-success/10 text-success"
                                        : run.status ===
                                        "RUNNING"
                                            ? "bg-warning/10 text-warning"
                                            : "bg-secondary text-muted"
                                }
                                `}
                            >
                                {t(
                                    `statuses.${run.status}`,
                                )}
                            </span>
                        </td>

                        <td className="whitespace-nowrap px-4 py-3 text-primary">
                            {run.processedCount.toLocaleString(
                                locale,
                            )}
                        </td>

                        <td className="whitespace-nowrap px-4 py-3 text-muted">
                            {formatDateTime(
                                run.startedAt,
                                locale,
                            )}
                        </td>

                        <td className="whitespace-nowrap px-4 py-3 text-muted">
                            {formatDateTime(
                                run.completedAt,
                                locale,
                            )}
                        </td>

                        <td className="whitespace-nowrap px-4 py-3 text-muted">
                            {formatDateTime(
                                run.createdAt,
                                locale,
                            )}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}