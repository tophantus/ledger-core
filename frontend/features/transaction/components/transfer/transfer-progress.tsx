"use client";

import {useTranslations} from "next-intl";

type TransferStep =
    | "TRANSFER"
    | "OTP"
    | "RESULT";

interface TransferProgressProps {
    step: TransferStep;
}

export function TransferProgress({
                                     step,
                                 }: TransferProgressProps) {
    const t =
        useTranslations("transaction");

    const steps: Array<{
        key: TransferStep;
        label: string;
    }> = [
        {
            key: "TRANSFER",
            label: t(
                "transfer.steps.transfer",
            ),
        },
        {
            key: "OTP",
            label: t(
                "transfer.steps.confirm",
            ),
        },
    ];

    const currentIndex =
        steps.findIndex(
            (item) =>
                item.key === step,
        );

    return (
        <div className="grid grid-cols-2 gap-2">
            {steps.map(
                (item, index) => {
                    const active =
                        index <=
                        currentIndex;

                    return (
                        <div
                            key={item.key}
                        >
                            <div
                                className={`
                                    h-1
                                    rounded-full
                                    ${
                                    active
                                        ? "bg-primary"
                                        : "bg-secondary"
                                }
                                `}
                            />

                            <p
                                className={`
                                    mt-2
                                    text-xs
                                    ${
                                    active
                                        ? "text-primary"
                                        : "text-muted"
                                }
                                `}
                            >
                                {
                                    item.label
                                }
                            </p>
                        </div>
                    );
                },
            )}
        </div>
    );
}