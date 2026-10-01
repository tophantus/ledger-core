package com.example.ledgercore.credit.command.dto;

import java.util.UUID;

public record RevokeCreditRepaymentMandateCommand(
        UUID userId,
        UUID mandateId
) {
}