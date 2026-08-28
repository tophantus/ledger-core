package com.example.ledgercore.transaction.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ConfirmTransferCommand(

        @NotNull
        UUID intentId,

        @NotBlank
        @Size(max = 10)
        String otp

) {
}