package com.example.ledgercore.otp.adapter.outbound;

import com.example.ledgercore.common.encryption.EncryptionService;
import com.example.ledgercore.otp.command.port.outbound.OtpSenderPort;
import com.example.ledgercore.otp.enums.OtpChannel;
import com.example.ledgercore.otp.enums.OtpPurpose;
import com.example.ledgercore.otp.event.OtpNotificationEvent;
import com.example.ledgercore.outbox.command.port.inbound.SaveOutboxEventUseCase;
import com.example.ledgercore.outbox.event.OutboxAggregateType;
import com.example.ledgercore.outbox.event.OutboxEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class OtpSenderAdapter implements OtpSenderPort {

    private final SaveOutboxEventUseCase saveOutboxEventUseCase;
    private final EncryptionService encryptionService;

    @Override
    public void send(
            UUID otpChallengeId,
            UUID subjectId,
            UUID referenceId,
            OtpPurpose purpose,
            OtpChannel channel,
            String destination,
            String otp
    ) {
        String encryptedOtp =
                encryptionService.encrypt(otp);

        log.debug("OTP: {}", otp);

        OtpNotificationEvent event =
                new OtpNotificationEvent(
                        otpChallengeId,
                        subjectId,
                        referenceId,
                        purpose,
                        channel,
                        destination,
                        encryptedOtp
                );

        saveOutboxEventUseCase.execute(
                OutboxAggregateType.OTP.getValue(),
                otpChallengeId,
                OutboxEventType.OTP_CHALLENGE_NOTIFICATION_REQUESTED.getValue(),
                event
        );
    }
}