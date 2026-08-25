package com.example.ledgercore.outbox.adapter.outbound.rabbit;

import com.example.ledgercore.otp.config.OtpRabbitConfig;
import com.example.ledgercore.outbox.entity.OutboxEvent;
import com.example.ledgercore.transaction.config.TransactionRabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventRabbitMapper {

    public RabbitOutboxMessage map(OutboxEvent event) {
        return new RabbitOutboxMessage(
                resolveExchange(event),
                resolveRoutingKey(event)
        );
    }

    private String resolveExchange(OutboxEvent event) {
        return switch (event.getAggregateType()) {
            case "OTP" ->
                    OtpRabbitConfig.OTP_EXCHANGE;

            case "TRANSACTION" ->
                    TransactionRabbitConfig.TRANSACTION_EXCHANGE;

            default -> throw new IllegalArgumentException(
                    "Unsupported aggregate type: "
                            + event.getAggregateType()
            );
        };
    }

    private String resolveRoutingKey(OutboxEvent event) {
        return switch (event.getEventType()) {
            case "OTP_CHALLENGE_NOTIFICATION_REQUESTED" ->
                    OtpRabbitConfig.OTP_NOTIFICATION_ROUTING_KEY;

            case "TRANSFER_COMPLETED" ->
                    TransactionRabbitConfig.TRANSFER_COMPLETED_ROUTING_KEY;

            case "DEPOSIT_COMPLETED" ->
                    TransactionRabbitConfig.DEPOSIT_COMPLETED_ROUTING_KEY;

            case "WITHDRAW_COMPLETED" ->
                    TransactionRabbitConfig.WITHDRAW_COMPLETED_ROUTING_KEY;

            default -> throw new IllegalArgumentException(
                    "Unsupported event type: "
                            + event.getEventType()
            );
        };
    }
}