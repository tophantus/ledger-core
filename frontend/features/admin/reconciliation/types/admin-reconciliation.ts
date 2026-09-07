import type {
    ApiResponse,
    PageResponse,
} from "@/lib/api/types";

export type ReconciliationType =
    | "TRANSACTION_JOURNAL"
    | "JOURNAL_BALANCE"
    | "ACCOUNT_BALANCE";

export type ReconciliationRunStatus =
    | "PENDING"
    | "RUNNING"
    | "COMPLETED";

export type ReconciliationTargetType =
    | "TRANSACTION"
    | "JOURNAL"
    | "ACCOUNT";

export type ReconciliationErrorCode =
    | "JOURNAL_NOT_FOUND"
    | "TRANSACTION_AMOUNT_MISMATCH"
    | "BUSINESS_DATE_MISMATCH"
    | "JOURNAL_NOT_BALANCED"
    | "BALANCE_MISMATCH"
    | "OPENING_BALANCE_MISMATCH";

export interface ReconciliationRunSummary {
    id: string;
    type: ReconciliationType;
    status: ReconciliationRunStatus;
    processedCount: number;
}

export interface ReconciliationSummary {
    businessDate: string;
    runs: ReconciliationRunSummary[];
}

export interface ReconciliationException {
    id: string;
    reconciliationRunId: string;
    businessDate: string;
    targetType: ReconciliationTargetType;
    targetId: string;
    errorCode: ReconciliationErrorCode;
    expectedValue: string;
    actualValue: string;
    message: string;
    createdAt: string;
}

export interface ReconciliationExceptionFilters {
    businessDate?: string;
    targetType?: ReconciliationTargetType;
    errorCode?: ReconciliationErrorCode;
    page?: number;
    size?: number;
}

export type ReconciliationSummaryResponse =
    ApiResponse<ReconciliationSummary>;

export type ReconciliationExceptionPageResponse =
    ApiResponse<
        PageResponse<ReconciliationException>
    >;