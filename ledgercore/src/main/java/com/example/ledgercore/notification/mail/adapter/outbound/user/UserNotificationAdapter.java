package com.example.ledgercore.notification.mail.adapter.outbound;

import com.example.ledgercore.notification.mail.command.port.outbound.UserNotificationPort;
import com.example.ledgercore.notification.mail.command.port.outbound.dto.UserNotificationInfo;
import com.example.ledgercore.otp.enums.OtpChannel;
import com.example.ledgercore.user.query.dto.UserNotificationResponse;
import com.example.ledgercore.user.query.port.inbound.GetUserByEmailUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserNotificationAdapter
        implements UserNotificationPort {

    private final GetUserByEmailUseCase getUserByEmailUseCase;

    @Override
    public UserNotificationInfo getUser(
            String destination,
            OtpChannel channel
    ) {
        return switch (channel) {

            case EMAIL ->
                    getByEmail(destination);

            case SMS ->
                    getByPhone(destination);
        };
    }

    private UserNotificationInfo getByEmail(
            String email
    ) {
        UserNotificationResponse user =
                getUserByEmailUseCase.execute(email);

        return new UserNotificationInfo(
                user.fullName()
        );
    }

    private UserNotificationInfo getByPhone(
            String phoneNumber
    ) {
        // TODO: implement when SMS notification is supported.
        throw new UnsupportedOperationException(
                "SMS user lookup is not implemented yet"
        );
    }
}