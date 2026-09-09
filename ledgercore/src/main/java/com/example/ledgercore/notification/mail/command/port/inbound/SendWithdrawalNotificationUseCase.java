package com.example.ledgercore.notification.mail.command.port.inbound;

import com.example.ledgercore.withdrawal.event.WithdrawalNotificationEvent;

public interface SendWithdrawalNotificationUseCase {

    void execute(
            WithdrawalNotificationEvent event
    );
}