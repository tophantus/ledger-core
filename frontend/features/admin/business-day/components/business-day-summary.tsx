"use client";

import {
    useCallback,
    useEffect,
    useState,
} from "react";
import {
    useLocale,
    useTranslations,
} from "next-intl";

import {Button} from "@/components/ui/button";
import {useAdminBusinessDay} from "../hooks/use-admin-business-day";
import type {
    CurrentBusinessDay,
} from "../types/admin-business-day";

export function BusinessDaySummary() {
    const t = useTranslations(
        "admin.dashboard.businessDay",
    );
    const tErrors = useTranslations("errors");
    const locale = useLocale();

    const {
        getCurrentBusinessDay,
        closeBusinessDay,
    } = useAdminBusinessDay();

    const [
        businessDay,
        setBusinessDay,
    ] = useState<CurrentBusinessDay | null>(
        null,
    );

    const [loading, setLoading] =
        useState(true);

    const [closing, setClosing] =
        useState(false);

    const [error, setError] =
        useState<string | null>(null);

    useEffect(() => {
        let cancelled = false;

        const loadCurrentBusinessDay =
            async () => {
                try {
                    const response =
                        await getCurrentBusinessDay();

                    if (cancelled) {
                        return;
                    }

                    if (!response.success) {
                        setError(
                            response.code &&
                            tErrors.has(
                                response.code,
                            )
                                ? tErrors(
                                    response.code,
                                )
                                : tErrors(
                                    "fallback",
                                ),
                        );
                        return;
                    }

                    setBusinessDay(
                        response.data,
                    );
                } catch {
                    if (!cancelled) {
                        setError(
                            tErrors("fallback"),
                        );
                    }
                } finally {
                    if (!cancelled) {
                        setLoading(false);
                    }
                }
            };

        void loadCurrentBusinessDay();

        return () => {
            cancelled = true;
        };
    }, [
        getCurrentBusinessDay,
        tErrors,
    ]);

    const fetchCurrentBusinessDay =
        useCallback(async () => {
            setError(null);
            setLoading(true);

            try {
                const response =
                    await getCurrentBusinessDay();

                if (!response.success) {
                    setError(
                        response.code &&
                        tErrors.has(
                            response.code,
                        )
                            ? tErrors(
                                response.code,
                            )
                            : tErrors(
                                "fallback",
                            ),
                    );
                    return;
                }

                setBusinessDay(
                    response.data,
                );
            } catch {
                setError(
                    tErrors("fallback"),
                );
            } finally {
                setLoading(false);
            }
        }, [
            getCurrentBusinessDay,
            tErrors,
        ]);

    const handleClose = async () => {
        if (
            closing ||
            !businessDay?.canClose
        ) {
            return;
        }

        setError(null);
        setClosing(true);

        try {
            const response =
                await closeBusinessDay();

            if (!response.success) {
                setError(
                    response.code &&
                    tErrors.has(
                        response.code,
                    )
                        ? tErrors(
                            response.code,
                        )
                        : tErrors(
                            "fallback",
                        ),
                );
                return;
            }

            await fetchCurrentBusinessDay();
        } catch {
            setError(
                tErrors("fallback"),
            );
        } finally {
            setClosing(false);
        }
    };

    const formattedDate =
        businessDay
            ? new Intl.DateTimeFormat(
                locale,
                {
                    year: "numeric",
                    month: "long",
                    day: "numeric",
                },
            ).format(
                new Date(
                    `${businessDay.businessDate}T00:00:00`,
                ),
            )
            : null;

    return (
        <section className="
            rounded-lg
            border
            border-border
            bg-surface
            p-6
        ">
            <div>
                <h2 className="
                    text-lg
                    font-semibold
                    text-primary
                ">
                    {t("title")}
                </h2>

                <p className="
                    mt-1
                    text-sm
                    text-muted
                ">
                    {t("description")}
                </p>
            </div>

            <div className="
                mt-6
                grid
                grid-cols-1
                gap-4
                md:grid-cols-2
            ">
                <div className="
                    rounded-md
                    border
                    border-border
                    bg-background
                    px-4
                    py-3
                ">
                    <p className="
                        text-sm
                        font-medium
                        text-foreground
                    ">
                        {t("currentDay")}
                    </p>
                </div>

                <div className="
                    rounded-md
                    border
                    border-border
                    bg-background
                    px-4
                    py-3
                ">
                    {loading ? (
                        <div className="
                            h-5
                            w-40
                            animate-pulse
                            rounded
                            bg-secondary
                        " />
                    ) : businessDay ? (
                        <p className="
                            text-sm
                            font-medium
                            text-foreground
                        ">
                            {formattedDate}
                        </p>
                    ) : (
                        <p className="
                            text-sm
                            text-muted
                        ">
                            -
                        </p>
                    )}
                </div>
            </div>

            {error && (
                <p className="
                    mt-3
                    text-sm
                    text-danger
                ">
                    {error}
                </p>
            )}

            {businessDay?.canClose && (
                <div className="
                    mt-4
                    flex
                    justify-end
                ">
                    <Button
                        type="button"
                        variant="danger"
                        loading={closing}
                        onClick={handleClose}
                    >
                        {t("close")}
                    </Button>
                </div>
            )}
        </section>
    );
}
