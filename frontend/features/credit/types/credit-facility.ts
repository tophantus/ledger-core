import type {ApiResponse} from "@/lib/api/types";
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
    currency: Currency;
    status: CreditFacilityStatus;
    openedAt: string;
}

export type CreditFacilityResponse =
    ApiResponse<GetUserCreditFacilityResult>;