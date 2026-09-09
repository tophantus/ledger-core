package com.example.ledgercore.notification.mail.command.port.outbound;

import com.example.ledgercore.notification.mail.command.port.outbound.dto.UserNotificationByIdInfo;
import com.example.ledgercore.notification.mail.command.port.outbound.dto.UserNotificationInfo;
import com.example.ledgercore.otp.enums.OtpChannel;

import java.util.UUID;

public interface UserNotificationPort {

    UserNotificationInfo getUserByDestination(
            String destination,
            OtpChannel channel
    );

    UserNotificationByIdInfo getUserById(
            UUID userId
    );
}