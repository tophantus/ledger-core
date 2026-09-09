package com.example.ledgercore.notification.mail.adapter.outbound.user;

import com.example.ledgercore.notification.mail.command.port.outbound.UserNotificationPort;
import com.example.ledgercore.notification.mail.command.port.outbound.dto.UserNotificationByIdInfo;
import com.example.ledgercore.notification.mail.command.port.outbound.dto.UserNotificationInfo;
import com.example.ledgercore.otp.enums.OtpChannel;
import com.example.ledgercore.user.query.dto.CurrentUserResponse;
import com.example.ledgercore.user.query.dto.UserNotificationResponse;
import com.example.ledgercore.user.query.port.inbound.GetCurrentUserUseCase;
import com.example.ledgercore.user.query.port.inbound.GetUserByEmailUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserNotificationAdapter
        implements UserNotificationPort {

    private final GetUserByEmailUseCase getUserByEmailUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    @Override
    public UserNotificationInfo getUserByDestination(
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

    @Override
    public UserNotificationByIdInfo getUserById(UUID userId) {
        CurrentUserResponse user =
                getCurrentUserUseCase.execute(userId);

        return new UserNotificationByIdInfo(
                user.email(),
                user.profile().fullName()

        );
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