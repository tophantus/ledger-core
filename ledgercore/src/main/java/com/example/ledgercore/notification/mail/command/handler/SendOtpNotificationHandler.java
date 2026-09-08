package com.example.ledgercore.notification.mail.command.handler;

import com.example.ledgercore.common.encryption.EncryptionService;
import com.example.ledgercore.notification.mail.command.port.inbound.SendOtpNotificationUseCase;
import com.example.ledgercore.notification.mail.command.port.outbound.TransferIntentQueryPort;
import com.example.ledgercore.notification.mail.command.port.outbound.UserNotificationPort;
import com.example.ledgercore.notification.mail.command.port.outbound.dto.TransferIntentNotificationInfo;
import com.example.ledgercore.notification.mail.command.port.outbound.dto.UserNotificationInfo;
import com.example.ledgercore.notification.mail.enums.EmailTemplateType;
import com.example.ledgercore.notification.mail.service.EmailNotificationService;
import com.example.ledgercore.notification.mail.service.MoneyFormatter;
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
    private final UserNotificationPort userNotificationPort;

    private final MoneyFormatter moneyFormatter;

    @Override
    public void execute(OtpNotificationEvent event) {
        validateChannel(event);

        EmailTemplateType templateType =
                resolveTemplate(event.purpose());

        String otp =
                encryptionService.decrypt(
                        event.encryptedOtp()
                );

        UserNotificationInfo user =
                userNotificationPort.getUserByDestination(
                        event.destination(),
                        event.channel()
                );

        Map<String, Object> variables =
                buildVariables(
                        event,
                        user.fullName(),
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
            String fullName,
            String otp
    ) {
        return switch (event.purpose()) {

            case CONFIRM_TRANSFER ->
                    buildTransferConfirmationVariables(
                            event,
                            fullName,
                            otp
                    );

            case EMAIL_VERIFICATION,
                 CONFIRM_WITHDRAWAL_REQUEST ->
                    buildOtpVariables(
                            event,
                            fullName,
                            otp
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported OTP purpose: "
                                    + event.purpose()
                    );
        };
    }

    private Map<String, Object> buildOtpVariables(
            OtpNotificationEvent event,
            String fullName,
            String otp
    ) {
        return Map.of(
                "otp",
                otp,

                "fullName",
                fullName,

                "expiresInMinutes",
                event.purpose()
                        .getExpiration()
                        .toMinutes()
        );
    }

    private Map<String, Object> buildTransferConfirmationVariables(
            OtpNotificationEvent event,
            String fullName,
            String otp
    ) {
        TransferIntentNotificationInfo intent =
                transferIntentQueryPort.getTransferIntent(
                        event.referenceId()
                );

        return Map.of(
                "otp", otp,
                "fullName", fullName,
                "expiresInMinutes",
                event.purpose()
                        .getExpiration()
                        .toMinutes(),
                "destinationAccountNo",
                intent.destinationAccountNo(),
                "amount",
                moneyFormatter.format(
                        intent.amount(),
                        intent.currency()
                ),
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

            case CONFIRM_WITHDRAWAL_REQUEST ->
                    EmailTemplateType.CONFIRM_WITHDRAWAL_REQUEST;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported OTP purpose: " + purpose
                    );
        };
    }
}