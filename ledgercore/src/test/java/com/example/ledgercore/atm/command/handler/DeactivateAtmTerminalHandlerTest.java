package com.example.ledgercore.atm.command.handler;

import com.example.ledgercore.atm.command.dto.DeactivateAtmTerminalCommand;
import com.example.ledgercore.atm.command.dto.DeactivateAtmTerminalResponse;
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
class DeactivateAtmTerminalHandlerTest {

    @Mock
    private AtmTerminalCommandRepository
            atmTerminalCommandRepository;

    private DeactivateAtmTerminalHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DeactivateAtmTerminalHandler(
                atmTerminalCommandRepository
        );
    }

    @Test
    void shouldDeactivateActiveAtmTerminalSuccessfully() {
        UUID terminalId = UUID.randomUUID();

        AtmTerminal terminal = createTerminal(
                terminalId,
                "ATM-HN-001",
                AtmTerminalStatus.ACTIVE
        );

        when(atmTerminalCommandRepository.findById(terminalId))
                .thenReturn(Optional.of(terminal));

        when(atmTerminalCommandRepository.save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        DeactivateAtmTerminalResponse response =
                handler.execute(
                        new DeactivateAtmTerminalCommand(
                                terminalId
                        )
                );

        assertThat(response)
                .isNotNull();

        assertThat(response.terminalId())
                .isEqualTo(terminalId);

        assertThat(response.terminalCode())
                .isEqualTo("ATM-HN-001");

        assertThat(response.status())
                .isEqualTo(AtmTerminalStatus.INACTIVE);

        assertThat(terminal.getStatus())
                .isEqualTo(AtmTerminalStatus.INACTIVE);

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
        DeactivateAtmTerminalCommand command =
                new DeactivateAtmTerminalCommand(null);

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

        when(atmTerminalCommandRepository.findById(terminalId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> handler.execute(
                        new DeactivateAtmTerminalCommand(
                                terminalId
                        )
                )
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
    void shouldRejectAlreadyInactiveAtmTerminal() {
        UUID terminalId = UUID.randomUUID();

        AtmTerminal terminal = createTerminal(
                terminalId,
                "ATM-HN-001",
                AtmTerminalStatus.INACTIVE
        );

        when(atmTerminalCommandRepository.findById(terminalId))
                .thenReturn(Optional.of(terminal));

        assertThatThrownBy(
                () -> handler.execute(
                        new DeactivateAtmTerminalCommand(
                                terminalId
                        )
                )
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(
                        ErrorCode.ATM_TERMINAL_ALREADY_INACTIVE
                );

        assertThat(terminal.getStatus())
                .isEqualTo(AtmTerminalStatus.INACTIVE);

        verify(atmTerminalCommandRepository)
                .findById(terminalId);

        verify(atmTerminalCommandRepository, never())
                .save(any());
    }

    @Test
    void shouldChangeOnlyStatusWhenDeactivating() {
        UUID terminalId = UUID.randomUUID();

        AtmTerminal terminal = createTerminal(
                terminalId,
                "ATM-HN-001",
                AtmTerminalStatus.ACTIVE
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
                new DeactivateAtmTerminalCommand(
                        terminalId
                )
        );

        assertThat(terminal.getStatus())
                .isEqualTo(AtmTerminalStatus.INACTIVE);

        assertThat(terminal.getCredentialHash())
                .isEqualTo(credentialHash);

        assertThat(terminal.getTerminalCode())
                .isEqualTo("ATM-HN-001");

        assertThat(terminal.getLocation())
                .isEqualTo(location);
    }

    @Test
    void shouldSaveDeactivatedTerminal() {
        UUID terminalId = UUID.randomUUID();

        AtmTerminal terminal = createTerminal(
                terminalId,
                "ATM-HN-001",
                AtmTerminalStatus.ACTIVE
        );

        when(atmTerminalCommandRepository.findById(terminalId))
                .thenReturn(Optional.of(terminal));

        when(atmTerminalCommandRepository.save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        handler.execute(
                new DeactivateAtmTerminalCommand(
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
                .isEqualTo(AtmTerminalStatus.INACTIVE);
    }

    @Test
    void shouldReturnDeactivatedTerminalResponse() {
        UUID terminalId = UUID.randomUUID();

        AtmTerminal terminal = createTerminal(
                terminalId,
                "ATM-HN-001",
                AtmTerminalStatus.ACTIVE
        );

        when(atmTerminalCommandRepository.findById(terminalId))
                .thenReturn(Optional.of(terminal));

        when(atmTerminalCommandRepository.save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        DeactivateAtmTerminalResponse response =
                handler.execute(
                        new DeactivateAtmTerminalCommand(
                                terminalId
                        )
                );

        assertThat(response.terminalId())
                .isEqualTo(terminalId);

        assertThat(response.terminalCode())
                .isEqualTo("ATM-HN-001");

        assertThat(response.status())
                .isEqualTo(AtmTerminalStatus.INACTIVE);
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