package com.example.ledgercore.atm.command.port.inbound;

import com.example.ledgercore.atm.command.dto.RegisterAtmTerminalCommand;
import com.example.ledgercore.atm.command.dto.RegisterAtmTerminalResponse;

public interface RegisterAtmTerminalUseCase {

    RegisterAtmTerminalResponse execute(
            RegisterAtmTerminalCommand command
    );
}