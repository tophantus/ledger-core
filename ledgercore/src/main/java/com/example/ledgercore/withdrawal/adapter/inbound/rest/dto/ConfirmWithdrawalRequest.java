package com.example.ledgercore.withdrawal.adapter.inbound.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmWithdrawalRequest(
        @NotBlank
        String otp
) {
}