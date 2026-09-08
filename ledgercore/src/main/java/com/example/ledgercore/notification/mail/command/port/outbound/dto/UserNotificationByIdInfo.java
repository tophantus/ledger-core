package com.example.ledgercore.notification.mail.command.port.outbound.dto;

public record UserNotificationByIdInfo(
        String email,
        String fullName
) {
}