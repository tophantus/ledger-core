import {useTranslations} from "next-intl";

import {InterestOverviewLink} from "./interest-overview-link";
import {ROUTES} from "@/lib/constants/routes";

interface InterestOverviewQuickLinksProps {
    t: ReturnType<typeof useTranslations>;
}

export function InterestOverviewQuickLinks({
                                               t,
                                           }: InterestOverviewQuickLinksProps) {
    return (
        <section>
            <h2 className="
                mb-3
                text-base
                font-semibold
                text-foreground
            ">
                {t("quickLinks")}
            </h2>

            <div className="
                grid
                gap-4
                md:grid-cols-3
            ">
                <InterestOverviewLink
                    href={ROUTES.ADMIN.INTERESTS.RUNS}
                    title={t("links.runs.title")}
                    description={t(
                        "links.runs.description",
                    )}
                />

                <InterestOverviewLink
                    href={ROUTES.ADMIN.INTERESTS.ACCRUALS}
                    title={t("links.accruals.title")}
                    description={t(
                        "links.accruals.description",
                    )}
                />

                <InterestOverviewLink
                    href={ROUTES.ADMIN.INTERESTS.POSTINGS}
                    title={t("links.postings.title")}
                    description={t(
                        "links.postings.description",
                    )}
                />
            </div>
        </section>
    );
}