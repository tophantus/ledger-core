package com.example.ledgercore.atm.command.handler;

import com.example.ledgercore.atm.command.dto.RotateAtmCredentialCommand;
import com.example.ledgercore.atm.command.dto.RotateAtmCredentialResponse;
import com.example.ledgercore.atm.command.port.inbound.RotateAtmCredentialUseCase;
import com.example.ledgercore.atm.command.repository.AtmTerminalCommandRepository;
import com.example.ledgercore.atm.command.service.AtmCredentialGenerator;
import com.example.ledgercore.atm.command.service.AtmCredentialHasher;
import com.example.ledgercore.atm.entity.AtmTerminal;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RotateAtmCredentialHandler
        implements RotateAtmCredentialUseCase {

    private final AtmTerminalCommandRepository
            atmTerminalCommandRepository;

    private final AtmCredentialGenerator
            atmCredentialGenerator;

    private final AtmCredentialHasher
            atmCredentialHasher;

    @Override
    @Transactional
    public RotateAtmCredentialResponse execute(
            RotateAtmCredentialCommand command
    ) {
        validateCommand(command);

        AtmTerminal terminal =
                atmTerminalCommandRepository
                        .findById(command.terminalId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.ATM_TERMINAL_NOT_FOUND
                                )
                        );

        String credential =
                atmCredentialGenerator.generate();

        String credentialHash =
                atmCredentialHasher.hash(credential);

        terminal.setCredentialHash(credentialHash);

        atmTerminalCommandRepository.save(terminal);

        return new RotateAtmCredentialResponse(
                terminal.getId(),
                terminal.getTerminalCode(),
                credential
        );
    }

    private void validateCommand(
            RotateAtmCredentialCommand command
    ) {
        if (command == null
                || command.terminalId() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}