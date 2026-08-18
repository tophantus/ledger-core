package com.example.ledgercore.auth.command.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordCommand(

        @NotBlank
        String currentPassword,

        @NotBlank
        String newPassword
) {
}