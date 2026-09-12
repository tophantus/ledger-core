import {Clock3} from "lucide-react";

import type {InterestRun} from "@/features/admin/interest/types/admin-interest";

interface InterestOverviewRunRowProps {
    run: InterestRun;
    t: ReturnType<typeof import("next-intl").useTranslations>;
}

export function InterestOverviewRunRow({
                                           run,
                                           t,
                                       }: InterestOverviewRunRowProps) {
    return (
        <tr className="
            border-b
            border-border
            last:border-b-0
        ">
            <td className="
                whitespace-nowrap
                px-4
                py-3
                text-foreground
            ">
                {run.businessDate}
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-3
                text-foreground
            ">
                {t(`runType.${run.runType}`)}
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-3
            ">
                <span className="
                    inline-flex
                    items-center
                    gap-1.5
                    text-sm
                ">
                    <Clock3 className="h-4 w-4" />

                    {t(`status.${run.status}`)}
                </span>
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-3
                text-foreground
            ">
                {run.processedCount}
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-3
                text-muted
            ">
                {formatDateTime(run.startedAt)}
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-3
                text-muted
            ">
                {formatDateTime(run.completedAt)}
            </td>
        </tr>
    );
}

function formatDateTime(
    value: string | null,
): string {
    if (!value) {
        return "—";
    }

    return new Date(value).toLocaleString();
}