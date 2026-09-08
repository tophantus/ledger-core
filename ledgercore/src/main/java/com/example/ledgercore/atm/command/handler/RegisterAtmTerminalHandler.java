package com.example.ledgercore.atm.command.handler;

import com.example.ledgercore.atm.command.dto.RegisterAtmTerminalCommand;
import com.example.ledgercore.atm.command.dto.RegisterAtmTerminalResponse;
import com.example.ledgercore.atm.command.port.inbound.RegisterAtmTerminalUseCase;
import com.example.ledgercore.atm.command.repository.AtmTerminalCommandRepository;
import com.example.ledgercore.atm.command.service.AtmCredentialGenerator;
import com.example.ledgercore.atm.command.service.AtmCredentialHasher;
import com.example.ledgercore.atm.entity.AtmTerminal;
import com.example.ledgercore.atm.enums.AtmTerminalStatus;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RegisterAtmTerminalHandler
        implements RegisterAtmTerminalUseCase {

    private final AtmTerminalCommandRepository
            atmTerminalCommandRepository;

    private final AtmCredentialGenerator
            atmCredentialGenerator;

    private final AtmCredentialHasher
            atmCredentialHasher;

    private final Clock clock;

    @Override
    @Transactional
    public RegisterAtmTerminalResponse execute(
            RegisterAtmTerminalCommand command
    ) {
        validateCommand(command);

        String terminalCode =
                command.terminalCode().trim();

        if (atmTerminalCommandRepository
                .existsByTerminalCode(terminalCode)) {

            throw new BusinessException(
                    ErrorCode.ATM_TERMINAL_ALREADY_EXISTS
            );
        }

        String credential =
                atmCredentialGenerator.generate();

        String credentialHash =
                atmCredentialHasher.hash(
                        credential
                );

        Instant now = Instant.now(clock);

        AtmTerminal terminal =
                AtmTerminal.builder()
                        .terminalCode(terminalCode)
                        .credentialHash(credentialHash)
                        .status(AtmTerminalStatus.ACTIVE)
                        .location(command.location())
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

        terminal =
                atmTerminalCommandRepository.save(
                        terminal
                );

        return new RegisterAtmTerminalResponse(
                terminal.getId(),
                terminal.getTerminalCode(),
                credential,
                terminal.getStatus(),
                terminal.getLocation(),
                terminal.getCreatedAt()
        );
    }

    private void validateCommand(
            RegisterAtmTerminalCommand command
    ) {
        if (command == null
                || command.terminalCode() == null
                || command.terminalCode().isBlank()) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.terminalCode().trim().length() > 50) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.location() != null
                && command.location().length() > 255) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}