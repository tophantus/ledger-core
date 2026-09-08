package com.example.ledgercore.notification.mail.command.port.outbound;

import com.example.ledgercore.notification.mail.command.port.outbound.dto.UserNotificationInfo;
import com.example.ledgercore.otp.enums.OtpChannel;

public interface UserNotificationPort {

    UserNotificationInfo getUser(
            String destination,
            OtpChannel channel
    );
}