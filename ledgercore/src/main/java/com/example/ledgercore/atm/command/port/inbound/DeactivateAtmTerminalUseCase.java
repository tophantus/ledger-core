package com.example.ledgercore.atm.command.port.inbound;

import com.example.ledgercore.atm.command.dto.DeactivateAtmTerminalCommand;
import com.example.ledgercore.atm.command.dto.DeactivateAtmTerminalResponse;

public interface DeactivateAtmTerminalUseCase {

    DeactivateAtmTerminalResponse execute(
            DeactivateAtmTerminalCommand command
    );
}