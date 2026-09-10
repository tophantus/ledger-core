package com.example.ledgercore.withdrawal.command.dto;

import java.math.BigDecimal;

public record ExecuteWithdrawalCommand(
        String terminalCode,
        String credential,
        String lookupCode,
        String withdrawalCode,
        BigDecimal amount
) {
}