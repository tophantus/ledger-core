import {Currency} from "@/lib/constants/currency";

export interface DepositMoneyRequest {
    destinationAccountId: string;
    amount: string;
    currency: Currency;
    reference: string;
    description?: string;
}