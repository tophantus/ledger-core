package com.example.ledgercore.atm.command.handler;

import com.example.ledgercore.atm.command.dto.AtmAuthenticationResult;
import com.example.ledgercore.atm.command.dto.AuthenticateAtmCommand;
import com.example.ledgercore.atm.command.port.inbound.AuthenticateAtmUseCase;
import com.example.ledgercore.atm.command.repository.AtmTerminalCommandRepository;
import com.example.ledgercore.atm.command.service.AtmCredentialHasher;
import com.example.ledgercore.atm.entity.AtmTerminal;
import com.example.ledgercore.atm.enums.AtmTerminalStatus;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticateAtmHandler
        implements AuthenticateAtmUseCase {

    private final AtmTerminalCommandRepository
            atmTerminalCommandRepository;

    private final AtmCredentialHasher
            atmCredentialHasher;

    @Override
    public AtmAuthenticationResult execute(
            AuthenticateAtmCommand command
    ) {
        validateCommand(command);

        AtmTerminal terminal =
                atmTerminalCommandRepository
                        .findByTerminalCode(
                                command.terminalCode()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.ATM_TERMINAL_NOT_FOUND
                                )
                        );

        validateTerminalStatus(terminal);

        validateCredential(
                command.credential(),
                terminal
        );

        return new AtmAuthenticationResult(
                terminal.getId()
        );
    }

    private void validateCommand(
            AuthenticateAtmCommand command
    ) {
        if (command == null
                || command.terminalCode() == null
                || command.terminalCode().isBlank()
                || command.credential() == null
                || command.credential().isBlank()) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateTerminalStatus(
            AtmTerminal terminal
    ) {
        if (terminal.getStatus()
                != AtmTerminalStatus.ACTIVE) {

            throw new BusinessException(
                    ErrorCode.ATM_TERMINAL_NOT_ACTIVE
            );
        }
    }

    private void validateCredential(
            String credential,
            AtmTerminal terminal
    ) {
        if (!atmCredentialHasher.matches(
                credential,
                terminal.getCredentialHash()
        )) {
            throw new BusinessException(
                    ErrorCode.ATM_AUTHENTICATION_FAILED
            );
        }
    }
}