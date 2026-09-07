import type {Transaction} from "@/features/transaction/types/transaction";
import type {ApiResponse} from "@/lib/api/types";

export interface DepositMoneyRequest {
    destinationAccountId: string;
    amount: string;
    currency: string;
    reference: string;
    description?: string;
}

export type DepositMoneyResponse =
    ApiResponse<Transaction>;