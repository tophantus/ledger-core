package com.example.ledgercore.user.adapter.inbound.auth;

import com.example.ledgercore.auth.command.port.outbound.UserAuthenticationPort;
import com.example.ledgercore.user.command.dto.ActivateUserCommand;
import com.example.ledgercore.user.command.dto.CreateUserCommand;
import com.example.ledgercore.user.command.port.inbound.ActivateUserUseCase;
import com.example.ledgercore.user.command.port.inbound.CreateUserUseCase;
import com.example.ledgercore.user.query.dto.UserAuthenticationResponse;
import com.example.ledgercore.user.query.port.inbound.GetUserAuthenticationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserAuthenticationAdapter
        implements UserAuthenticationPort {

    private final CreateUserUseCase createUserUseCase;
    private final ActivateUserUseCase activateUserUseCase;
    private final GetUserAuthenticationUseCase
            getUserAuthenticationUseCase;

    @Override
    public Optional<UserAuthenticationInfo> findByEmail(
            String email
    ) {
        return getUserAuthenticationUseCase
                .findByEmail(email)
                .map(this::toAuthenticationInfo);
    }

    @Override
    public Optional<UserAuthenticationInfo> findById(
            UUID userId
    ) {
        return getUserAuthenticationUseCase
                .findById(userId)
                .map(this::toAuthenticationInfo);
    }

    @Override
    public Optional<UserAuthenticationInfo> createUser(
            CreateUserData data
    ) {
        return createUserUseCase.execute(
                new CreateUserCommand(
                        data.username(),
                        data.email(),
                        data.passwordHash()
                )
        );
    }

    @Override
    public void activateUser(
            UUID userId
    ) {
        activateUserUseCase.execute(
                new ActivateUserCommand(userId)
        );
    }

    private UserAuthenticationInfo toAuthenticationInfo(
            UserAuthenticationResponse response
    ) {
        return new UserAuthenticationInfo(
                response.userId(),
                response.username(),
                response.email(),
                response.passwordHash(),
                response.roles(),
                response.active()
        );
    }
}