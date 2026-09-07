"use client";

import {
    AlertCircle,
    Loader2,
    Search,
} from "lucide-react";
import {useTranslations} from "next-intl";

import type {
    ReconciliationException,
} from "../types/admin-reconciliation";

interface ReconciliationExceptionTableProps {
    exceptions: ReconciliationException[];
    loading: boolean;
    error: boolean;
}

export function ReconciliationExceptionTable({
                                                 exceptions,
                                                 loading,
                                                 error,
                                             }: ReconciliationExceptionTableProps) {
    const t = useTranslations(
        "admin.reconciliation",
    );

    if (loading) {
        return (
            <div className="flex items-center justify-center gap-2 p-8 text-sm text-muted">
                <Loader2 className="h-4 w-4 animate-spin" />
                {t("exceptions.loading")}
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex items-center gap-3 p-8 text-sm text-danger">
                <AlertCircle className="h-5 w-5 shrink-0" />
                {t("exceptions.loadError")}
            </div>
        );
    }

    if (exceptions.length === 0) {
        return (
            <div className="flex items-center justify-center gap-2 p-8 text-sm text-muted">
                <Search className="h-4 w-4" />
                {t("exceptions.empty")}
            </div>
        );
    }

    return (
        <div className="overflow-x-auto">
            <table className="w-full text-sm">
                <thead>
                <tr className="border-b border-border text-left">
                    <th className="px-4 py-3 font-medium text-muted">
                        {t(
                            "exceptions.table.targetType",
                        )}
                    </th>

                    <th className="px-4 py-3 font-medium text-muted">
                        {t(
                            "exceptions.table.targetId",
                        )}
                    </th>

                    <th className="px-4 py-3 font-medium text-muted">
                        {t(
                            "exceptions.table.errorCode",
                        )}
                    </th>

                    <th className="px-4 py-3 font-medium text-muted">
                        {t(
                            "exceptions.table.expected",
                        )}
                    </th>

                    <th className="px-4 py-3 font-medium text-muted">
                        {t(
                            "exceptions.table.actual",
                        )}
                    </th>

                    <th className="px-4 py-3 font-medium text-muted">
                        {t(
                            "exceptions.table.message",
                        )}
                    </th>

                    <th className="px-4 py-3 font-medium text-muted">
                        {t(
                            "exceptions.table.createdAt",
                        )}
                    </th>
                </tr>
                </thead>

                <tbody>
                {exceptions.map(
                    (exception) => (
                        <tr
                            key={exception.id}
                            className="border-b border-border last:border-0"
                        >
                            <td className="px-4 py-4 font-medium text-primary">
                                {t(
                                    `targetTypes.${exception.targetType}`,
                                )}
                            </td>

                            <td className="px-4 py-4">
                                    <span className="break-all font-mono text-xs text-primary">
                                        {
                                            exception.targetId
                                        }
                                    </span>
                            </td>

                            <td className="px-4 py-4">
                                    <span className="whitespace-nowrap text-xs font-medium text-primary">
                                        {t(
                                            `errorCodes.${exception.errorCode}`,
                                        )}
                                    </span>
                            </td>

                            <td className="px-4 py-4 font-mono text-xs text-primary">
                                {
                                    exception.expectedValue
                                }
                            </td>

                            <td className="px-4 py-4 font-mono text-xs text-primary">
                                {
                                    exception.actualValue
                                }
                            </td>

                            <td className="max-w-xs px-4 py-4 text-muted">
                                {
                                    exception.message
                                }
                            </td>

                            <td className="whitespace-nowrap px-4 py-4 text-muted">
                                {formatDate(
                                    exception.createdAt,
                                )}
                            </td>
                        </tr>
                    ),
                )}
                </tbody>
            </table>
        </div>
    );
}

function formatDate(
    value: string,
): string {
    return new Date(value).toLocaleString();
}