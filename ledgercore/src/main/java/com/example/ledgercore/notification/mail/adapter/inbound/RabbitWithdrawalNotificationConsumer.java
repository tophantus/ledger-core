package com.example.ledgercore.notification.mail.adapter.inbound;

import com.example.ledgercore.notification.mail.command.port.inbound.SendWithdrawalNotificationUseCase;
import com.example.ledgercore.notification.mail.config.WithdrawalCodeMailRabbitConfig;
import com.example.ledgercore.withdrawal.event.WithdrawalNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitWithdrawalNotificationConsumer {

    private final SendWithdrawalNotificationUseCase
            sendWithdrawalNotificationUseCase;

    @RabbitListener(
            queues = WithdrawalCodeMailRabbitConfig
                    .WITHDRAWAL_CODE_MAIL_QUEUE
    )
    public void consume(
            WithdrawalNotificationEvent event
    ) {
        log.info(
                "Received withdrawal notification " +
                        "withdrawalIntentId={}",
                event.withdrawalIntentId()
        );

        sendWithdrawalNotificationUseCase.execute(event);
    }
}