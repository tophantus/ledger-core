package com.example.ledgercore.card.adapter.inbound.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CaptureCardPaymentRequest(
        @NotNull
        UUID authorizationId,

        @NotBlank
        String reference,

        String description
) {
}
