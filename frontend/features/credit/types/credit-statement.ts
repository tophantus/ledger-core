import type {ApiResponse} from "@/lib/api/types";
import type {PageResponse} from "@/lib/api/types";

export enum CreditStatementStatus {
    OPEN = "OPEN",
    ISSUED = "ISSUED",
    PARTIALLY_PAID = "PARTIALLY_PAID",
    PAID = "PAID",
    OVERDUE = "OVERDUE",
    NO_PAYMENT_DUE = "NO_PAYMENT_DUE",
}

export interface CreditStatement {
    id: string;
    creditFacilityId: string;
    periodStart: string;
    periodEnd: string;
    statementDate: string;
    dueDate: string;
    openingBalance: string;
    purchasesAmount: string;
    paymentsAmount: string;
    feesAmount: string;
    interestAmount: string;
    postedInterestAmount: string;
    closingBalance: string;
    minimumPayment: string;
    paidAmount: string;
    status: CreditStatementStatus;
}

export type GetCreditStatementsResponse =
    ApiResponse<PageResponse<CreditStatement>>;