package com.example.ledgercore.auth.command.port.inbound;

import com.example.ledgercore.auth.command.dto.LogoutCommand;
import com.example.ledgercore.auth.command.dto.LogoutResponse;

public interface LogoutUseCase {

    LogoutResponse execute(LogoutCommand command);
}