package com.example.ledgercore.atm.command.handler;

import com.example.ledgercore.atm.command.dto.DeactivateAtmTerminalCommand;
import com.example.ledgercore.atm.command.dto.DeactivateAtmTerminalResponse;
import com.example.ledgercore.atm.command.port.inbound.DeactivateAtmTerminalUseCase;
import com.example.ledgercore.atm.command.repository.AtmTerminalCommandRepository;
import com.example.ledgercore.atm.entity.AtmTerminal;
import com.example.ledgercore.atm.enums.AtmTerminalStatus;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeactivateAtmTerminalHandler
        implements DeactivateAtmTerminalUseCase {

    private final AtmTerminalCommandRepository
            atmTerminalCommandRepository;

    @Override
    @Transactional
    public DeactivateAtmTerminalResponse execute(
            DeactivateAtmTerminalCommand command
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

        if (terminal.getStatus()
                == AtmTerminalStatus.INACTIVE) {

            throw new BusinessException(
                    ErrorCode.ATM_TERMINAL_ALREADY_INACTIVE
            );
        }

        terminal.setStatus(
                AtmTerminalStatus.INACTIVE
        );

        atmTerminalCommandRepository.save(terminal);

        return new DeactivateAtmTerminalResponse(
                terminal.getId(),
                terminal.getTerminalCode(),
                terminal.getStatus()
        );
    }

    private void validateCommand(
            DeactivateAtmTerminalCommand command
    ) {
        if (command == null
                || command.terminalId() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}