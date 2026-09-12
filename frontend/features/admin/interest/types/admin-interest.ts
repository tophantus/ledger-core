import type {Currency} from "@/lib/constants/currency";

export type InterestRunType =
    | "ACCRUAL"
    | "POSTING";

export type InterestRunStatus =
    | "PENDING"
    | "RUNNING"
    | "COMPLETED";

export interface InterestRun {
    id: string;
    businessDate: string;
    runType: InterestRunType;
    status: InterestRunStatus;
    processedCount: number;
    startedAt: string | null;
    completedAt: string | null;
    createdAt: string;
}

export interface InterestAccrual {
    id: string;
    accountId: string;
    currency: Currency;
    businessDate: string;
    interestConfigId: string;
    principalAmount: string;
    interestRate: string;
    interestAmount: string;
    journalEntryId: string | null;
    postingId: string | null;
    runId: string;
    createdAt: string;
}

export interface InterestPosting {
    id: string;
    accountId: string;
    runId: string;
    periodStart: string;
    periodEnd: string;
    interestAmount: string;
    transactionId: string;
    postedAt: string;
    createdAt: string;
}

export interface InterestRunFilters {
    businessDate?: string;
    fromDate?: string;
    toDate?: string;
    runType?: InterestRunType;
    status?: InterestRunStatus;
    page?: number;
    size?: number;
}

export interface InterestAccrualFilters {
    runId?: string;
    businessDate?: string;
    accountId?: string;
    page?: number;
    size?: number;
}

export interface InterestPostingFilters {
    runId?: string;
    businessDate?: string;
    accountId?: string;
    page?: number;
    size?: number;
}