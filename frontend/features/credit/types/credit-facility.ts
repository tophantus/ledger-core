import type {Currency} from "@/lib/constants/currency";

import {
    CreditRepaymentMandateStatus,
    RepaymentType,
} from "./credit-repayment-mandate";

export enum CreditFacilityStatus {
    ACTIVE = "ACTIVE",
    SUSPENDED = "SUSPENDED",
    CLOSED = "CLOSED",
}

export interface CreditRepaymentMandate {
    id: string;
    accountId: string;
    repaymentType: RepaymentType;
    status: CreditRepaymentMandateStatus;
    createdAt: string;
    updatedAt: string;
}

export interface GetUserCreditFacilityResult {
    id: string;
    customerId: string;
    productId: string;
    creditLimit: string;
    outstandingBalance: string;
    holdAmount: string;
    availableCredit: string;
    currency: Currency;
    status: CreditFacilityStatus;
    openedAt: string;
    repaymentMandate: CreditRepaymentMandate | null;
}