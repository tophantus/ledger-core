package com.example.ledgercore.atm.command.handler;

import com.example.ledgercore.atm.command.dto.ActivateAtmTerminalCommand;
import com.example.ledgercore.atm.command.dto.ActivateAtmTerminalResponse;
import com.example.ledgercore.atm.command.repository.AtmTerminalCommandRepository;
import com.example.ledgercore.atm.entity.AtmTerminal;
import com.example.ledgercore.atm.enums.AtmTerminalStatus;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivateAtmTerminalHandlerTest {

    @Mock
    private AtmTerminalCommandRepository
            atmTerminalCommandRepository;

    private ActivateAtmTerminalHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ActivateAtmTerminalHandler(
                atmTerminalCommandRepository
        );
    }

    @Test
    void shouldActivateInactiveAtmTerminalSuccessfully() {
        UUID terminalId = UUID.randomUUID();

        AtmTerminal terminal = createTerminal(
                terminalId,
                "ATM-HN-001",
                AtmTerminalStatus.INACTIVE
        );

        ActivateAtmTerminalCommand command =
                new ActivateAtmTerminalCommand(
                        terminalId
                );

        when(atmTerminalCommandRepository.findById(terminalId))
                .thenReturn(Optional.of(terminal));

        when(atmTerminalCommandRepository.save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        ActivateAtmTerminalResponse response =
                handler.execute(command);

        assertThat(response)
                .isNotNull();

        assertThat(response.terminalId())
                .isEqualTo(terminalId);

        assertThat(response.terminalCode())
                .isEqualTo("ATM-HN-001");

        assertThat(response.status())
                .isEqualTo(AtmTerminalStatus.ACTIVE);

        assertThat(terminal.getStatus())
                .isEqualTo(AtmTerminalStatus.ACTIVE);

        verify(atmTerminalCommandRepository)
                .findById(terminalId);

        verify(atmTerminalCommandRepository)
                .save(terminal);
    }

    @Test
    void shouldRejectNullCommand() {
        assertThatThrownBy(
                () -> handler.execute(null)
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(
                atmTerminalCommandRepository
        );
    }

    @Test
    void shouldRejectNullTerminalId() {
        ActivateAtmTerminalCommand command =
                new ActivateAtmTerminalCommand(null);

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(
                atmTerminalCommandRepository
        );
    }

    @Test
    void shouldThrowWhenAtmTerminalNotFound() {
        UUID terminalId = UUID.randomUUID();

        ActivateAtmTerminalCommand command =
                new ActivateAtmTerminalCommand(
                        terminalId
                );

        when(atmTerminalCommandRepository.findById(terminalId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(
                        ErrorCode.ATM_TERMINAL_NOT_FOUND
                );

        verify(atmTerminalCommandRepository)
                .findById(terminalId);

        verify(atmTerminalCommandRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectAlreadyActiveAtmTerminal() {
        UUID terminalId = UUID.randomUUID();

        AtmTerminal terminal = createTerminal(
                terminalId,
                "ATM-HN-001",
                AtmTerminalStatus.ACTIVE
        );

        ActivateAtmTerminalCommand command =
                new ActivateAtmTerminalCommand(
                        terminalId
                );

        when(atmTerminalCommandRepository.findById(terminalId))
                .thenReturn(Optional.of(terminal));

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(
                        ErrorCode.ATM_TERMINAL_ALREADY_ACTIVE
                );

        assertThat(terminal.getStatus())
                .isEqualTo(AtmTerminalStatus.ACTIVE);

        verify(atmTerminalCommandRepository)
                .findById(terminalId);

        verify(atmTerminalCommandRepository, never())
                .save(any());
    }

    @Test
    void shouldChangeOnlyStatusWhenActivating() {
        UUID terminalId = UUID.randomUUID();

        AtmTerminal terminal = createTerminal(
                terminalId,
                "ATM-HN-001",
                AtmTerminalStatus.INACTIVE
        );

        String credentialHash =
                terminal.getCredentialHash();

        String location =
                terminal.getLocation();

        when(atmTerminalCommandRepository.findById(terminalId))
                .thenReturn(Optional.of(terminal));

        when(atmTerminalCommandRepository.save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        handler.execute(
                new ActivateAtmTerminalCommand(
                        terminalId
                )
        );

        assertThat(terminal.getStatus())
                .isEqualTo(AtmTerminalStatus.ACTIVE);

        assertThat(terminal.getCredentialHash())
                .isEqualTo(credentialHash);

        assertThat(terminal.getTerminalCode())
                .isEqualTo("ATM-HN-001");

        assertThat(terminal.getLocation())
                .isEqualTo(location);
    }

    @Test
    void shouldSaveActivatedTerminal() {
        UUID terminalId = UUID.randomUUID();

        AtmTerminal terminal = createTerminal(
                terminalId,
                "ATM-HN-001",
                AtmTerminalStatus.INACTIVE
        );

        when(atmTerminalCommandRepository.findById(terminalId))
                .thenReturn(Optional.of(terminal));

        when(atmTerminalCommandRepository.save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        handler.execute(
                new ActivateAtmTerminalCommand(
                        terminalId
                )
        );

        ArgumentCaptor<AtmTerminal> captor =
                ArgumentCaptor.forClass(
                        AtmTerminal.class
                );

        verify(atmTerminalCommandRepository)
                .save(captor.capture());

        AtmTerminal savedTerminal =
                captor.getValue();

        assertThat(savedTerminal.getId())
                .isEqualTo(terminalId);

        assertThat(savedTerminal.getTerminalCode())
                .isEqualTo("ATM-HN-001");

        assertThat(savedTerminal.getStatus())
                .isEqualTo(AtmTerminalStatus.ACTIVE);
    }

    @Test
    void shouldReturnActivatedTerminalResponse() {
        UUID terminalId = UUID.randomUUID();

        AtmTerminal terminal = createTerminal(
                terminalId,
                "ATM-HN-001",
                AtmTerminalStatus.INACTIVE
        );

        when(atmTerminalCommandRepository.findById(terminalId))
                .thenReturn(Optional.of(terminal));

        when(atmTerminalCommandRepository.save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        ActivateAtmTerminalResponse response =
                handler.execute(
                        new ActivateAtmTerminalCommand(
                                terminalId
                        )
                );

        assertThat(response.terminalId())
                .isEqualTo(terminalId);

        assertThat(response.terminalCode())
                .isEqualTo("ATM-HN-001");

        assertThat(response.status())
                .isEqualTo(AtmTerminalStatus.ACTIVE);
    }

    private AtmTerminal createTerminal(
            UUID id,
            String terminalCode,
            AtmTerminalStatus status
    ) {
        return AtmTerminal.builder()
                .id(id)
                .terminalCode(terminalCode)
                .credentialHash("credential-hash")
                .status(status)
                .location("Hanoi")
                .build();
    }
}