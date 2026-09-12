import {
    CheckCircle2,
    Percent,
    PlayCircle,
} from "lucide-react";

import type {InterestRun} from "@/features/admin/interest/types/admin-interest";

type SummaryCardIcon =
    | "run"
    | "accrual"
    | "posting";

interface InterestOverviewSummaryCardProps {
    title: string;
    run?: InterestRun;
    loading: boolean;
    icon: SummaryCardIcon;
    t: ReturnType<typeof import("next-intl").useTranslations>;
}

const ICONS = {
    run: PlayCircle,
    accrual: Percent,
    posting: CheckCircle2,
};

export function InterestOverviewSummaryCard({
                                                title,
                                                run,
                                                loading,
                                                icon,
                                                t,
                                            }: InterestOverviewSummaryCardProps) {
    const Icon = ICONS[icon];

    if (loading) {
        return (
            <div className="
                rounded-lg
                border
                border-border
                bg-surface
                p-5
            ">
                <div className="
                    h-4
                    w-28
                    animate-pulse
                    rounded
                    bg-background-subtle"
                />

                <div className="
                     mt-4
                     h-7
                     w-24
                     animate-pulse
                     rounded
                     bg-background-subtle"
                />

                <div className="
                    mt-2
                    h-4
                    w-32
                    animate-pulse
                    rounded
                    bg-background-subtle
                " />
            </div>
        );
    }

    return (
        <div className="
            rounded-lg
            border
            border-border
            bg-surface
            p-5
        ">
            <div className="
                flex
                items-center
                justify-between
            ">
                <span className="
                    text-sm
                    font-medium
                    text-muted
                ">
                    {title}
                </span>

                <Icon className="
                    h-5
                    w-5
                    text-primary
                " />
            </div>

            {run ? (
                <>
                    <div className="
                        mt-4
                        text-lg
                        font-semibold
                        text-foreground
                    ">
                        {run.businessDate}
                    </div>

                    <div className="
                        mt-1
                        flex
                        items-center
                        gap-2
                        text-sm
                        text-muted
                    ">
                        <span>
                            {t(`status.${run.status}`)}
                        </span>

                        <span>·</span>

                        <span>
                            {run.processedCount}
                        </span>

                        <span>
                            {t("processed")}
                        </span>
                    </div>
                </>
            ) : (
                <div className="
                    mt-4
                    text-sm
                    text-muted
                ">
                    {t("noRun")}
                </div>
            )}
        </div>
    );
}