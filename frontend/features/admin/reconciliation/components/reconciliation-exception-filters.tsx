"use client";

import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";

import type {
    ReconciliationErrorCode,
    ReconciliationTargetType,
} from "../types/admin-reconciliation";

interface ReconciliationExceptionFiltersProps {
    targetType:
        | ReconciliationTargetType
        | "";

    errorCode:
        | ReconciliationErrorCode
        | "";

    onTargetTypeChange: (
        value:
            | ReconciliationTargetType
            | "",
    ) => void;

    onErrorCodeChange: (
        value:
            | ReconciliationErrorCode
            | "",
    ) => void;

    onClear: () => void;
}

const TARGET_TYPES: ReconciliationTargetType[] = [
    "TRANSACTION",
    "JOURNAL",
    "ACCOUNT",
];

const ERROR_CODES: ReconciliationErrorCode[] = [
    "JOURNAL_NOT_FOUND",
    "TRANSACTION_AMOUNT_MISMATCH",
    "BUSINESS_DATE_MISMATCH",
    "JOURNAL_NOT_BALANCED",
    "BALANCE_MISMATCH",
    "OPENING_BALANCE_MISMATCH",
];

export function ReconciliationExceptionFilters({
                                                   targetType,
                                                   errorCode,
                                                   onTargetTypeChange,
                                                   onErrorCodeChange,
                                                   onClear,
                                               }: ReconciliationExceptionFiltersProps) {
    const t = useTranslations(
        "admin.reconciliation",
    );

    return (
        <div className="flex flex-col gap-4 border-b border-border p-4 md:flex-row md:items-end">
            <div className="flex-1 space-y-2">
                <label
                    htmlFor="target-type"
                    className="text-sm font-medium text-primary"
                >
                    {t(
                        "exceptions.filters.targetType",
                    )}
                </label>

                <select
                    id="target-type"
                    value={targetType}
                    onChange={(event) =>
                        onTargetTypeChange(
                            event.target
                                .value as
                                | ReconciliationTargetType
                                | "",
                        )
                    }
                    className="h-10 w-full rounded-md border border-border bg-background px-3 text-sm text-primary outline-none focus:ring-2 focus:ring-primary/20"
                >
                    <option value="">
                        {t(
                            "exceptions.filters.allTargetTypes",
                        )}
                    </option>

                    {TARGET_TYPES.map(
                        (type) => (
                            <option
                                key={type}
                                value={type}
                            >
                                {t(
                                    `targetTypes.${type}`,
                                )}
                            </option>
                        ),
                    )}
                </select>
            </div>

            <div className="flex-1 space-y-2">
                <label
                    htmlFor="error-code"
                    className="text-sm font-medium text-primary"
                >
                    {t(
                        "exceptions.filters.errorCode",
                    )}
                </label>

                <select
                    id="error-code"
                    value={errorCode}
                    onChange={(event) =>
                        onErrorCodeChange(
                            event.target
                                .value as
                                | ReconciliationErrorCode
                                | "",
                        )
                    }
                    className="h-10 w-full rounded-md border border-border bg-background px-3 text-sm text-primary outline-none focus:ring-2 focus:ring-primary/20"
                >
                    <option value="">
                        {t(
                            "exceptions.filters.allErrorCodes",
                        )}
                    </option>

                    {ERROR_CODES.map(
                        (code) => (
                            <option
                                key={code}
                                value={code}
                            >
                                {t(
                                    `errorCodes.${code}`,
                                )}
                            </option>
                        ),
                    )}
                </select>
            </div>

            {(targetType || errorCode) && (
                <Button
                    type="button"
                    variant="outline"
                    onClick={onClear}
                >
                    {t(
                        "exceptions.filters.clear",
                    )}
                </Button>
            )}
        </div>
    );
}