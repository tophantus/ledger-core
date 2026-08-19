package com.example.ledgercore.otp.adapter.outbound;

import com.example.ledgercore.common.encryption.EncryptionService;
import com.example.ledgercore.otp.command.port.outbound.OtpSenderPort;
import com.example.ledgercore.otp.enums.OtpChannel;
import com.example.ledgercore.otp.enums.OtpPurpose;
import com.example.ledgercore.otp.event.OtpNotificationEvent;
import com.example.ledgercore.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OtpSenderAdapter implements OtpSenderPort {

    private static final String AGGREGATE_TYPE = "OTP_CHALLENGE";
    private static final String EVENT_TYPE = "OTP_NOTIFICATION_REQUESTED";

    private final OutboxService outboxService;
    private final EncryptionService encryptionService;

    @Override
    public void send(
            UUID otpChallengeId,
            OtpPurpose purpose,
            OtpChannel channel,
            String destination,
            String otp
    ) {
        String encryptedOtp =
                encryptionService.encrypt(otp);

        OtpNotificationEvent event =
                new OtpNotificationEvent(
                        otpChallengeId,
                        purpose,
                        channel,
                        destination,
                        encryptedOtp
                );

        outboxService.save(
                AGGREGATE_TYPE,
                otpChallengeId,
                EVENT_TYPE,
                event
        );
    }
}