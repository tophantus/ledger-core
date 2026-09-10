"use client";

import {useTranslations} from "next-intl";

import type {
    WithdrawalIntentStatus,
} from "../../types/withdrawal";

interface WithdrawalListFiltersProps {
    status:
        | WithdrawalIntentStatus
        | undefined;

    onStatusChange: (
        status:
            | WithdrawalIntentStatus
            | undefined,
    ) => void;
}

const STATUSES: WithdrawalIntentStatus[] = [
    "READY",
    "COMPLETED",
    "EXPIRED",
    "CANCELLED",
];

export function WithdrawalListFilters({
                                          status,
                                          onStatusChange,
                                      }: WithdrawalListFiltersProps) {
    const t =
        useTranslations("withdrawal");

    return (
        <div className="
            rounded-lg
            border
            border-border
            bg-surface
            p-4
        ">
            <div className="
                flex
                flex-wrap
                items-end
                gap-4
            ">
                <div className="w-full sm:w-56">
                    <label
                        htmlFor="withdrawal-status"
                        className="
                            mb-2
                            block
                            text-sm
                            font-medium
                            text-foreground
                        "
                    >
                        {t("list.status")}
                    </label>

                    <select
                        id="withdrawal-status"
                        value={status ?? ""}
                        onChange={(event) => {
                            const value =
                                event.target.value;

                            onStatusChange(
                                value
                                    ? (value as WithdrawalIntentStatus)
                                    : undefined,
                            );
                        }}
                        className="
                            w-full
                            rounded-md
                            border
                            border-border
                            bg-background
                            px-3
                            py-2.5
                            text-sm
                            text-foreground
                            outline-none
                        "
                    >
                        <option value="">
                            {t(
                                "list.allStatuses",
                            )}
                        </option>

                        {STATUSES.map(
                            (item) => (
                                <option
                                    key={item}
                                    value={item}
                                >
                                    {t(
                                        `statuses.${item}`,
                                    )}
                                </option>
                            ),
                        )}
                    </select>
                </div>
            </div>
        </div>
    );
}