package com.example.ledgercore.withdrawal.command.port.outbound;

import java.util.UUID;

public interface WithdrawalOtpPort {

    void sendConfirmationOtp(
            UUID userId,
            UUID withdrawalRequestId
    );

    void verifyConfirmationOtp(
            UUID userId,
            UUID withdrawalRequestId,
            String otp
    );
}