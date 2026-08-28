package com.example.ledgercore.notification.mail.command.handler;

import com.example.ledgercore.common.encryption.EncryptionService;
import com.example.ledgercore.notification.mail.command.port.inbound.SendOtpNotificationUseCase;
import com.example.ledgercore.notification.mail.command.port.outbound.TransferIntentQueryPort;
import com.example.ledgercore.notification.mail.command.port.outbound.dto.TransferIntentNotificationInfo;
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
    private final TransferIntentQueryPort transferIntentQueryPort;

    @Override
    public void execute(OtpNotificationEvent event) {
        validateChannel(event);

        EmailTemplateType templateType =
                resolveTemplate(event.purpose());

        String otp =
                encryptionService.decrypt(
                        event.encryptedOtp()
                );

        Map<String, Object> variables =
                buildVariables(
                        event,
                        otp
                );

        emailNotificationService.send(
                event.destination(),
                templateType,
                variables
        );
    }

    private Map<String, Object> buildVariables(
            OtpNotificationEvent event,
            String otp
    ) {
        if (event.purpose() == OtpPurpose.CONFIRM_TRANSFER) {
            return buildTransferConfirmationVariables(
                    event,
                    otp
            );
        }

        return Map.of(
                "otp", otp,
                "expiresInMinutes",
                event.purpose()
                        .getExpiration()
                        .toMinutes()
        );
    }

    private Map<String, Object> buildTransferConfirmationVariables(
            OtpNotificationEvent event,
            String otp
    ) {
        TransferIntentNotificationInfo intent =
                transferIntentQueryPort.getTransferIntent(
                        event.referenceId()
                );

        return Map.of(
                "otp", otp,
                "expiresInMinutes",
                event.purpose()
                        .getExpiration()
                        .toMinutes(),
                "destinationAccountNo",
                intent.destinationAccountNo(),
                "amount",
                intent.amount(),
                "currency",
                intent.currency(),
                "reference",
                intent.reference(),
                "description",
                intent.description() == null
                        ? ""
                        : intent.description()
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

            case CONFIRM_TRANSFER ->
                    EmailTemplateType.TRANSFER_CONFIRMATION;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported OTP purpose: " + purpose
                    );
        };
    }
}