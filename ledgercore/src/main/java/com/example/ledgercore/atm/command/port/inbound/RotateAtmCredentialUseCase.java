package com.example.ledgercore.atm.command.port.inbound;

import com.example.ledgercore.atm.command.dto.RotateAtmCredentialCommand;
import com.example.ledgercore.atm.command.dto.RotateAtmCredentialResponse;

public interface RotateAtmCredentialUseCase {

    RotateAtmCredentialResponse execute(
            RotateAtmCredentialCommand command
    );
}