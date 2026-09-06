"use client";

import {transactionApi} from "../api/transaction-api";
import type {
    WithdrawMoneyRequest,
} from "../types/transaction";

export function useWithdrawMoney() {
    const withdrawMoney = async (
        request: WithdrawMoneyRequest,
    ) => {
        return transactionApi.withdraw(
            request,
        );
    };

    return {
        withdrawMoney,
    };
}