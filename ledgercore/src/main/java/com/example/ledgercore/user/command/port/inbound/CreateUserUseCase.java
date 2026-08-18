package com.example.ledgercore.user.command.port.inbound;

import com.example.ledgercore.user.command.dto.CreateUserCommand;
import com.example.ledgercore.user.query.dto.UserAuthenticationResponse;

import java.util.Optional;

public interface CreateUserUseCase {

    Optional<UserAuthenticationResponse> execute(CreateUserCommand command);
}