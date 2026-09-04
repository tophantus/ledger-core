package com.example.ledgercore.outbox.event;

import com.example.ledgercore.businessday.config.BusinessDayRabbitConfig;
import com.example.ledgercore.businessday.event.BusinessDayClosedEvent;
import com.example.ledgercore.otp.config.OtpRabbitConfig;
import com.example.ledgercore.otp.event.OtpNotificationEvent;
import com.example.ledgercore.transaction.config.TransactionRabbitConfig;
import com.example.ledgercore.transaction.event.DepositCompletedEvent;
import com.example.ledgercore.transaction.event.TransferCompletedEvent;
import com.example.ledgercore.transaction.event.WithdrawCompletedEvent;
import lombok.Getter;

@Getter
public enum OutboxEventType {

    // =========================
    // OTP
    // =========================

    OTP_CHALLENGE_NOTIFICATION_REQUESTED(
            "OTP_CHALLENGE_NOTIFICATION_REQUESTED",
            OutboxAggregateType.OTP,
            OtpNotificationEvent.class,
            OtpRabbitConfig.OTP_EXCHANGE,
            OtpRabbitConfig.OTP_NOTIFICATION_ROUTING_KEY
    ),

    // =========================
    // TRANSACTION
    // =========================

    TRANSFER_COMPLETED(
            "TRANSFER_COMPLETED",
            OutboxAggregateType.TRANSACTION,
            TransferCompletedEvent.class,
            TransactionRabbitConfig.TRANSACTION_EXCHANGE,
            TransactionRabbitConfig.TRANSFER_COMPLETED_ROUTING_KEY
    ),

    DEPOSIT_COMPLETED(
            "DEPOSIT_COMPLETED",
            OutboxAggregateType.TRANSACTION,
            DepositCompletedEvent.class,
            TransactionRabbitConfig.TRANSACTION_EXCHANGE,
            TransactionRabbitConfig.DEPOSIT_COMPLETED_ROUTING_KEY
    ),

    WITHDRAW_COMPLETED(
            "WITHDRAW_COMPLETED",
            OutboxAggregateType.TRANSACTION,
            WithdrawCompletedEvent.class,
            TransactionRabbitConfig.TRANSACTION_EXCHANGE,
            TransactionRabbitConfig.WITHDRAW_COMPLETED_ROUTING_KEY
    ),

    // =========================
    // BUSINESS DAY
    // =========================

    BUSINESS_DAY_CLOSED(
            "BUSINESS_DAY_CLOSED",
            OutboxAggregateType.BUSINESS_DAY,
            BusinessDayClosedEvent.class,
            BusinessDayRabbitConfig.BUSINESS_DAY_EXCHANGE,
            BusinessDayRabbitConfig.BUSINESS_DAY_CLOSED_ROUTING_KEY
            );

    private final String value;
    private final OutboxAggregateType aggregateType;
    private final Class<?> payloadType;
    private final String exchange;
    private final String routingKey;

    OutboxEventType(
            String value,
            OutboxAggregateType aggregateType,
            Class<?> payloadType,
            String exchange,
            String routingKey
    ) {
        this.value = value;
        this.aggregateType = aggregateType;
        this.payloadType = payloadType;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public static OutboxEventType fromValue(String value) {
        for (OutboxEventType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException(
                "Unknown outbox event type: " + value
        );
    }
}