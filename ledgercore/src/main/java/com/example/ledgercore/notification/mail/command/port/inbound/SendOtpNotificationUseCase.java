package com.example.ledgercore.notification.mail.command.port.inbound;

import com.example.ledgercore.otp.event.OtpNotificationEvent;

public interface SendOtpNotificationUseCase {

    void execute(OtpNotificationEvent event);
}