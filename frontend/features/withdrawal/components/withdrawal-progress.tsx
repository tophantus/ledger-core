"use client";

import {Check} from "lucide-react";
import {useTranslations} from "next-intl";

type WithdrawalStep =
    | "WITHDRAWAL"
    | "OTP"
    | "RESULT";

interface WithdrawalProgressProps {
    step: WithdrawalStep;
}

const STEPS: WithdrawalStep[] = [
    "WITHDRAWAL",
    "OTP",
    "RESULT",
];

export function WithdrawalProgress({
                                       step,
                                   }: WithdrawalProgressProps) {
    const t = useTranslations("withdrawal");

    const currentIndex =
        STEPS.indexOf(step);

    return (
        <div className="flex items-center gap-2">
            {STEPS.map((item, index) => {
                const completed =
                    index < currentIndex;

                const active =
                    index === currentIndex;

                return (
                    <div
                        key={item}
                        className="flex min-w-0 flex-1 items-center gap-2"
                    >
                        <div
                            className={`
                                flex
                                h-8
                                w-8
                                shrink-0
                                items-center
                                justify-center
                                rounded-full
                                text-xs
                                font-semibold
                                ${
                                completed ||
                                active
                                    ? "bg-primary text-primary-foreground"
                                    : "bg-background-subtle text-muted"
                            }
                            `}
                        >
                            {completed ? (
                                <Check className="h-4 w-4" />
                            ) : (
                                index + 1
                            )}
                        </div>

                        <span
                            className={`
                                truncate
                                text-xs
                                font-medium
                                ${
                                active
                                    ? "text-primary"
                                    : "text-muted"
                            }
                            `}
                        >
                            {t(
                                `steps.${item.toLowerCase()}`,
                            )}
                        </span>

                        {index <
                            STEPS.length - 1 && (
                                <div
                                    className={`
                                    h-px
                                    min-w-4
                                    flex-1
                                    ${
                                        index <
                                        currentIndex
                                            ? "bg-primary"
                                            : "bg-border"
                                    }
                                `}
                                />
                            )}
                    </div>
                );
            })}
        </div>
    );
}