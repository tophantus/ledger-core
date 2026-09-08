package com.example.ledgercore.withdrawal.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WithdrawalRabbitConfig {

    public static final String WITHDRAWAL_EXCHANGE =
            "withdrawal.exchange";

    public static final String WITHDRAWAL_CODE_NOTIFICATION_ROUTING_KEY =
            "withdrawal.code.notification";
}