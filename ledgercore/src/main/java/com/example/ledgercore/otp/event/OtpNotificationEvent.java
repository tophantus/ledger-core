package com.example.ledgercore.otp.event;

import com.example.ledgercore.otp.enums.OtpChannel;
import com.example.ledgercore.otp.enums.OtpPurpose;

import java.util.UUID;

public record OtpNotificationEvent(
        UUID otpChallengeId,
        OtpPurpose purpose,
        OtpChannel channel,
        String destination,
        String otp
) {
}