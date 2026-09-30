package com.example.ledgercore.credit.messaging.repayment;

import java.util.UUID;

public record RepaymentJob(
        UUID statementId
) {
}