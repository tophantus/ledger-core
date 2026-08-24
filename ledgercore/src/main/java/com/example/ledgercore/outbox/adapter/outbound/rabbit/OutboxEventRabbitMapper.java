package com.example.ledgercore.outbox.adapter.outbound.rabbit;

import com.example.ledgercore.otp.config.OtpRabbitConfig;
import com.example.ledgercore.outbox.entity.OutboxEvent;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventRabbitMapper {

    public RabbitOutboxMessage map(OutboxEvent event) {

        return new RabbitOutboxMessage(
                resolveExchange(event),
                resolveRoutingKey(event)
        );
    }

    private String resolveExchange(OutboxEvent event) {
        return switch (event.getAggregateType()) {
            case "OTP" -> OtpRabbitConfig.OTP_EXCHANGE;
            case "ACCOUNT" -> "account.exchange";
            case "TRANSACTION" -> "transaction.exchange";
            case "USER" -> "user.exchange";
            default -> throw new IllegalArgumentException(
                    "Unsupported aggregate type: "
                            + event.getAggregateType()
            );
        };
    }

    private String resolveRoutingKey(OutboxEvent event) {
        return switch (event.getEventType()) {
            case "OTP_CHALLENGE_NOTIFICATION_REQUESTED" -> OtpRabbitConfig.OTP_NOTIFICATION_ROUTING_KEY;

            case "ACCOUNT_BALANCE_CHANGED" ->
                    "account.balance.changed";

            case "TRANSFER_COMPLETED" ->
                    "transaction.transfer.completed";

            case "DEPOSIT_COMPLETED" ->
                    "transaction.deposit.completed";

            case "WITHDRAW_COMPLETED" ->
                    "transaction.withdraw.completed";

            default -> throw new IllegalArgumentException(
                    "Unsupported event type: "
                            + event.getEventType()
            );
        };
    }
}