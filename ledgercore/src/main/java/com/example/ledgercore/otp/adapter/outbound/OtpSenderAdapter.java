package com.example.ledgercore.otp.adapter.outbound;

import com.example.ledgercore.common.encryption.EncryptionService;
import com.example.ledgercore.otp.command.port.outbound.OtpSenderPort;
import com.example.ledgercore.otp.enums.OtpChannel;
import com.example.ledgercore.otp.enums.OtpPurpose;
import com.example.ledgercore.otp.event.OtpNotificationEvent;
import com.example.ledgercore.outbox.command.port.inbound.SaveOutboxEventUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OtpSenderAdapter implements OtpSenderPort {

    private static final String AGGREGATE_TYPE = "OTP";
    private static final String EVENT_TYPE = "OTP_CHALLENGE_NOTIFICATION_REQUESTED";

    private final SaveOutboxEventUseCase saveOutboxEventUseCase;
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

        System.out.println("OTP: " + otp);
        OtpNotificationEvent event =
                new OtpNotificationEvent(
                        otpChallengeId,
                        purpose,
                        channel,
                        destination,
                        encryptedOtp
                );

        saveOutboxEventUseCase.execute(
                AGGREGATE_TYPE,
                otpChallengeId,
                EVENT_TYPE,
                event
        );
    }
}