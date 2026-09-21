import type {
    ApiResponse,
    PageResponse,
} from "@/lib/api/types";

export enum ReconciliationType {
    TRANSACTION_JOURNAL = "TRANSACTION_JOURNAL",
    JOURNAL_BALANCE = "JOURNAL_BALANCE",
    ACCOUNT_BALANCE = "ACCOUNT_BALANCE",
}

export enum ReconciliationRunStatus {
    PENDING = "PENDING",
    RUNNING = "RUNNING",
    COMPLETED = "COMPLETED",
}

export enum ReconciliationTargetType {
    TRANSACTION = "TRANSACTION",
    JOURNAL = "JOURNAL",
    ACCOUNT = "ACCOUNT",
}

export enum ReconciliationErrorCode {
    JOURNAL_NOT_FOUND = "JOURNAL_NOT_FOUND",
    TRANSACTION_AMOUNT_MISMATCH =
        "TRANSACTION_AMOUNT_MISMATCH",
    BUSINESS_DATE_MISMATCH =
        "BUSINESS_DATE_MISMATCH",
    JOURNAL_NOT_BALANCED =
        "JOURNAL_NOT_BALANCED",
    BALANCE_MISMATCH = "BALANCE_MISMATCH",
    OPENING_BALANCE_MISMATCH =
        "OPENING_BALANCE_MISMATCH",
}

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