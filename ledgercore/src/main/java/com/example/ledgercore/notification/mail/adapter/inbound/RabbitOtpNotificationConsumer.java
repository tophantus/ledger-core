package com.example.ledgercore.notification.mail.adapter.inbound;

import com.example.ledgercore.notification.mail.command.port.inbound.SendOtpNotificationUseCase;
import com.example.ledgercore.notification.mail.config.MailRabbitConfig;
import com.example.ledgercore.otp.event.OtpNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitOtpNotificationConsumer {

    private final SendOtpNotificationUseCase
            sendOtpNotificationUseCase;

    @RabbitListener(
            queues = MailRabbitConfig.MAIL_QUEUE
    )
    public void consume(OtpNotificationEvent event) {
        log.info(
                "Received OTP notification otpChallengeId={}",
                event.otpChallengeId()
        );

        sendOtpNotificationUseCase.execute(event);
    }
}