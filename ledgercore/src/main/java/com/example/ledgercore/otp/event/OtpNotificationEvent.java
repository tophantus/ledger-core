package com.example.ledgercore.otp.event;

import com.example.ledgercore.otp.enums.OtpChannel;
import com.example.ledgercore.otp.enums.OtpPurpose;

import java.util.UUID;

public record OtpNotificationEvent(
        UUID otpChallengeId,
        UUID subjectId,
        UUID referenceId,
        OtpPurpose purpose,
        OtpChannel channel,
        String destination,
        String encryptedOtp
) {
}