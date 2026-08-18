package com.example.ledgercore.auth.command.port.inbound;

import com.example.ledgercore.auth.command.dto.SignUpCommand;
import com.example.ledgercore.auth.command.dto.SignUpResponse;

public interface SignUpUseCase {

    SignUpResponse execute(SignUpCommand command);
}