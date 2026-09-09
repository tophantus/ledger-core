package com.example.ledgercore.atm.command.port.inbound;

import com.example.ledgercore.atm.command.dto.AtmAuthenticationResult;
import com.example.ledgercore.atm.command.dto.AuthenticateAtmCommand;

public interface AuthenticateAtmUseCase {

    AtmAuthenticationResult execute(
            AuthenticateAtmCommand command
    );
}