package com.example.ledgercore.otp.command.dto;

import com.example.ledgercore.otp.enums.OtpChannel;
import com.example.ledgercore.otp.enums.OtpPurpose;

import java.util.UUID;

public record SendOtpCommand(
        UUID subjectId,
        UUID referenceId,
        OtpPurpose purpose,
        OtpChannel channel,
        String destination
) {
}