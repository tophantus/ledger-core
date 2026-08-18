package com.example.ledgercore.otp.command.port.outbound;

import com.example.ledgercore.otp.enums.OtpChannel;
import com.example.ledgercore.otp.enums.OtpPurpose;

import java.util.UUID;

public interface OtpSenderPort {

    void send(
            UUID otpChallengeId,
            OtpPurpose purpose,
            OtpChannel channel,
            String destination,
            String otp
    );
}