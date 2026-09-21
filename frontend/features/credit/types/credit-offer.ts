import type {ApiResponse} from "@/lib/api/types";
import type {Currency} from "@/lib/constants/currency";

export enum CreditOfferStatus {
    OFFERED = "OFFERED",
    ACCEPTED = "ACCEPTED",
    EXPIRED = "EXPIRED",
    REJECTED = "REJECTED",
    CANCELLED = "CANCELLED",
}

export interface CreditOfferInfo {
    offerId: string;
    customerId: string;
    creditFacilityId: string;
    productId: string;
    approvedLimit: string;
    currency: Currency;
    status: CreditOfferStatus;
    expiresAt: string;
    createdAt: string;
}

export interface AcceptCreditOfferResult {
    offerId: string;
    creditFacilityId: string;
    customerId: string;
    productId: string;
    creditLimit: string;
    currency: Currency;
    offerStatus: CreditOfferStatus;
    acceptedAt: string;
}

export interface RejectCreditOfferResult {
    offerId: string;
    customerId: string;
    status: CreditOfferStatus;
    rejectedAt: string;
}

export type CreditOfferResponse =
    ApiResponse<CreditOfferInfo>;

export type AcceptCreditOfferResponse =
    ApiResponse<AcceptCreditOfferResult>;

export type RejectCreditOfferResponse =
    ApiResponse<RejectCreditOfferResult>;