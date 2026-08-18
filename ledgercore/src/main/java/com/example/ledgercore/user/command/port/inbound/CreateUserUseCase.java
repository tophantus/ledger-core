package com.example.ledgercore.user.command.port.inbound;

import com.example.ledgercore.user.command.dto.CreateUserCommand;

import java.util.UUID;

public interface CreateUserUseCase {

    UUID execute(CreateUserCommand command);
}