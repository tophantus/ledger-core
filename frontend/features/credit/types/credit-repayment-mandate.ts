import type {ApiResponse} from "@/lib/api/types";

export enum RepaymentType {
    FULL_BALANCE = "FULL_BALANCE",
    MINIMUM_PAYMENT = "MINIMUM_PAYMENT",
}

export enum CreditRepaymentMandateStatus {
    ACTIVE = "ACTIVE",
    REVOKED = "REVOKED",
}

export interface CreateCreditRepaymentMandateRequest {
    creditFacilityId: string;
    accountId: string;
    repaymentType: RepaymentType;
}

export interface CreateCreditRepaymentMandateResult {
    mandateId: string;
    creditFacilityId: string;
    accountId: string;
    repaymentType: RepaymentType;
    status: CreditRepaymentMandateStatus;
    createdAt: string;
}

export interface UpdateCreditRepaymentMandateRequest {
    repaymentType: RepaymentType;
}

export interface UpdateCreditRepaymentMandateResult {
    mandateId: string;
    creditFacilityId: string;
    accountId: string;
    repaymentType: RepaymentType;
    status: CreditRepaymentMandateStatus;
    updatedAt: string;
}

export interface RevokeCreditRepaymentMandateResult {
    mandateId: string;
    creditFacilityId: string;
    accountId: string;
    status: CreditRepaymentMandateStatus;
    revokedAt: string;
}

export type CreateCreditRepaymentMandateResponse =
    ApiResponse<CreateCreditRepaymentMandateResult>;

export type UpdateCreditRepaymentMandateResponse =
    ApiResponse<UpdateCreditRepaymentMandateResult>;

export type RevokeCreditRepaymentMandateResponse =
    ApiResponse<RevokeCreditRepaymentMandateResult>;