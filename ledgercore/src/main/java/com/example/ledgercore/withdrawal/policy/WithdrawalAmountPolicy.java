package com.example.ledgercore.withdrawal.policy;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;

import java.math.BigDecimal;

public final class WithdrawalAmountPolicy {

    private WithdrawalAmountPolicy() {
    }

    public static void validate(
            BigDecimal amount,
            Currency currency
    ) {
        if (amount == null || currency == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        BigDecimal denomination =
                switch (currency) {
                    case VND -> new BigDecimal("50000");
                    case USD -> new BigDecimal("20");
                };

        if (amount.remainder(denomination).signum() != 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_WITHDRAW_AMOUNT
            );
        }
    }
}