package com.example.ledgercore.notification.mail.command.handler;

import com.example.ledgercore.common.encryption.EncryptionService;
import com.example.ledgercore.notification.mail.command.port.inbound.SendOtpNotificationUseCase;
import com.example.ledgercore.notification.mail.enums.EmailTemplateType;
import com.example.ledgercore.notification.mail.service.EmailNotificationService;
import com.example.ledgercore.otp.enums.OtpChannel;
import com.example.ledgercore.otp.enums.OtpPurpose;
import com.example.ledgercore.otp.event.OtpNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SendOtpNotificationHandler
        implements SendOtpNotificationUseCase {

    private final EmailNotificationService emailNotificationService;
    private final EncryptionService encryptionService;

    @Override
    public void execute(OtpNotificationEvent event) {
        validateChannel(event);

        EmailTemplateType templateType =
                resolveTemplate(event.purpose());

        String otp =
                encryptionService.decrypt(
                        event.encryptedOtp()
                );

        emailNotificationService.send(
                event.destination(),
                templateType,
                Map.of(
                        "otp", otp,
                        "expiresInMinutes",
                        event.purpose().getExpiration()
                )
        );
    }

    private void validateChannel(
            OtpNotificationEvent event
    ) {
        if (event.channel() != OtpChannel.EMAIL) {
            throw new IllegalArgumentException(
                    "Unsupported OTP notification channel: "
                            + event.channel()
            );
        }
    }

    private EmailTemplateType resolveTemplate(
            OtpPurpose purpose
    ) {
        return switch (purpose) {
            case EMAIL_VERIFICATION ->
                    EmailTemplateType.EMAIL_VERIFICATION;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported OTP purpose: " + purpose
                    );
        };
    }
}