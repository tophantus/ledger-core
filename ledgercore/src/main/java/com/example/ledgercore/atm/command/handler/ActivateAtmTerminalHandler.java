package com.example.ledgercore.atm.command.handler;

import com.example.ledgercore.atm.command.dto.ActivateAtmTerminalCommand;
import com.example.ledgercore.atm.command.dto.ActivateAtmTerminalResponse;
import com.example.ledgercore.atm.command.port.inbound.ActivateAtmTerminalUseCase;
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
public class ActivateAtmTerminalHandler
        implements ActivateAtmTerminalUseCase {

    private final AtmTerminalCommandRepository
            atmTerminalCommandRepository;

    @Override
    @Transactional
    public ActivateAtmTerminalResponse execute(
            ActivateAtmTerminalCommand command
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
                == AtmTerminalStatus.ACTIVE) {

            throw new BusinessException(
                    ErrorCode.ATM_TERMINAL_ALREADY_ACTIVE
            );
        }

        terminal.setStatus(
                AtmTerminalStatus.ACTIVE
        );

        atmTerminalCommandRepository.save(terminal);

        return new ActivateAtmTerminalResponse(
                terminal.getId(),
                terminal.getTerminalCode(),
                terminal.getStatus()
        );
    }

    private void validateCommand(
            ActivateAtmTerminalCommand command
    ) {
        if (command == null
                || command.terminalId() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}