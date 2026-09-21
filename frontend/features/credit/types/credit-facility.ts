import type {Currency} from "@/lib/constants/currency";

export enum CreditFacilityStatus {
    ACTIVE = "ACTIVE",
    SUSPENDED = "SUSPENDED",
    CLOSED = "CLOSED",
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
}