package com.example.ledgercore.atm.command.port.inbound;

import com.example.ledgercore.atm.command.dto.ActivateAtmTerminalCommand;
import com.example.ledgercore.atm.command.dto.ActivateAtmTerminalResponse;

public interface ActivateAtmTerminalUseCase {

    ActivateAtmTerminalResponse execute(
            ActivateAtmTerminalCommand command
    );
}