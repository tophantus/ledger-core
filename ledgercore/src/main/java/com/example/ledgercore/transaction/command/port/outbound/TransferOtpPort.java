package com.example.ledgercore.transaction.command.port.outbound;

import java.util.UUID;

public interface TransferOtpPort {

    void sendConfirmationOtp(
            UUID userId,
            UUID transferIntentId
    );
}