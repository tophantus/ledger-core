package com.example.ledgercore.otp.command.dto;

import com.example.ledgercore.otp.enums.OtpPurpose;

import java.util.UUID;

public record VerifyOtpCommand(
        UUID subjectId,
        UUID referenceId,
        OtpPurpose purpose,
        String otp
) {
}