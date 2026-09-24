import type {ApiResponse} from "@/lib/api/types";

export enum ProviderType {
    PSP = "PSP",
    ACQUIRER = "ACQUIRER",
}

export enum ProviderStatus {
    ACTIVE = "ACTIVE",
    INACTIVE = "INACTIVE",
}

export interface RegisterPaymentProviderRequest {
    code: string;
    name: string;
    type: ProviderType;
}

export interface RegisterPaymentProviderResult {
    providerId: string;
    code: string;
    name: string;
    type: ProviderType;
    status: ProviderStatus;
    clientId: string;
    credential: string;
}

export type RegisterPaymentProviderResponse =
    ApiResponse<RegisterPaymentProviderResult>;