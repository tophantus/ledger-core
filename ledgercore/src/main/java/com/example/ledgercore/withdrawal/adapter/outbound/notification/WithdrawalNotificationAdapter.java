package com.example.ledgercore.withdrawal.adapter.outbound.notification;

import com.example.ledgercore.common.encryption.EncryptionService;
import com.example.ledgercore.outbox.command.port.inbound.SaveOutboxEventUseCase;
import com.example.ledgercore.outbox.event.OutboxAggregateType;
import com.example.ledgercore.outbox.event.OutboxEventType;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalNotificationPort;
import com.example.ledgercore.withdrawal.event.WithdrawalNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WithdrawalNotificationAdapter
        implements WithdrawalNotificationPort {

    private final SaveOutboxEventUseCase saveOutboxEventUseCase;
    private final EncryptionService encryptionService;

    @Override
    public void sendWithdrawalCode(
            UUID withdrawalIntentId,
            UUID userId,
            String withdrawalReference,
            String withdrawalCode,
            BigDecimal amount,
            String currency,
            Instant expiresAt
    ) {
        String encryptedWithdrawalCode =
                encryptionService.encrypt(withdrawalCode);

        WithdrawalNotificationEvent event =
                new WithdrawalNotificationEvent(
                        withdrawalIntentId,
                        userId,
                        withdrawalReference,
                        amount,
                        currency,
                        encryptedWithdrawalCode,
                        expiresAt
                );

        saveOutboxEventUseCase.execute(
                OutboxAggregateType.WITHDRAWAL.getValue(),
                withdrawalIntentId,
                OutboxEventType
                        .WITHDRAWAL_CODE_NOTIFICATION_REQUESTED
                        .getValue(),
                event
        );
    }
}