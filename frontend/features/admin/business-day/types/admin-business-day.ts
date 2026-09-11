import type {ApiResponse} from "@/lib/api/types";

export type BusinessDayStatus =
    | "OPEN"
    | "CLOSED";

export interface CurrentBusinessDay {
    businessDate: string;
    status: BusinessDayStatus;
    canClose: boolean;
}

export type CurrentBusinessDayApiResponse =
    ApiResponse<CurrentBusinessDay>;