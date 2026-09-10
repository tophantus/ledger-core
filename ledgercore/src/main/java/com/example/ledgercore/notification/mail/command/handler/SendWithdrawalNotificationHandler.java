package com.example.ledgercore.notification.mail.command.handler;

import com.example.ledgercore.common.encryption.EncryptionService;
import com.example.ledgercore.notification.mail.command.port.inbound.SendWithdrawalNotificationUseCase;
import com.example.ledgercore.notification.mail.command.port.outbound.UserNotificationPort;
import com.example.ledgercore.notification.mail.command.port.outbound.dto.UserNotificationByIdInfo;
import com.example.ledgercore.notification.mail.enums.EmailTemplateType;
import com.example.ledgercore.notification.mail.service.EmailNotificationService;
import com.example.ledgercore.notification.mail.service.MoneyFormatter;
import com.example.ledgercore.withdrawal.event.WithdrawalNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SendWithdrawalNotificationHandler
        implements SendWithdrawalNotificationUseCase {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "dd MMMM yyyy 'at' hh:mm a",
                    Locale.ENGLISH
            );

    private final EmailNotificationService emailNotificationService;
    private final EncryptionService encryptionService;
    private final UserNotificationPort userNotificationPort;
    private final MoneyFormatter moneyFormatter;

    @Override
    public void execute(
            WithdrawalNotificationEvent event
    ) {
        validateEvent(event);

        String withdrawalCode =
                encryptionService.decrypt(
                        event.encryptedWithdrawalCode()
                );

        UserNotificationByIdInfo user =
                userNotificationPort.getUserById(
                        event.userId()
                );

        Map<String, Object> variables =
                buildVariables(
                        event,
                        user.fullName(),
                        withdrawalCode
                );

        emailNotificationService.send(
                user.email(),
                EmailTemplateType.WITHDRAWAL_CODE,
                variables
        );
    }

    private Map<String, Object> buildVariables(
            WithdrawalNotificationEvent event,
            String fullName,
            String withdrawalCode
    ) {

        String expiresAt =
                event.expiresAt()
                        .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                        .format(DATE_TIME_FORMATTER);

        return Map.of(
                "fullName",
                fullName,

                "withdrawalLookupCode",
                event.withdrawalLookupCode(),

                "withdrawalCode",
                withdrawalCode,

                "amount",
                moneyFormatter.format(
                        event.amount(),
                        event.currency()
                ),

                "expiresAt",
                expiresAt
        );
    }

    private void validateEvent(
            WithdrawalNotificationEvent event
    ) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Withdrawal notification event must not be null"
            );
        }

        if (event.encryptedWithdrawalCode() == null
                || event.encryptedWithdrawalCode().isBlank()) {
            throw new IllegalArgumentException(
                    "Encrypted withdrawal code must not be blank"
            );
        }

        if (event.withdrawalLookupCode() == null
                || event.withdrawalLookupCode().isBlank()) {
            throw new IllegalArgumentException(
                    "Withdrawal reference must not be blank"
            );
        }
    }
}