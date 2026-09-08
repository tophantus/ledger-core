package com.example.ledgercore.atm.command.handler;

import com.example.ledgercore.atm.command.dto.RotateAtmCredentialCommand;
import com.example.ledgercore.atm.command.dto.RotateAtmCredentialResponse;
import com.example.ledgercore.atm.command.repository.AtmTerminalCommandRepository;
import com.example.ledgercore.atm.command.service.AtmCredentialGenerator;
import com.example.ledgercore.atm.command.service.AtmCredentialHasher;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RotateAtmCredentialHandlerTest {

    @Mock
    private AtmTerminalCommandRepository
            atmTerminalCommandRepository;

    @Mock
    private AtmCredentialGenerator
            atmCredentialGenerator;

    @Mock
    private AtmCredentialHasher
            atmCredentialHasher;

    private RotateAtmCredentialHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RotateAtmCredentialHandler(
                atmTerminalCommandRepository,
                atmCredentialGenerator,
                atmCredentialHasher
        );
    }

    @Test
    void shouldRotateAtmCredentialSuccessfully() {
        UUID terminalId = UUID.randomUUID();

        AtmTerminal terminal =
                createTerminal(
                        terminalId,
                        "ATM-HN-001",
                        AtmTerminalStatus.ACTIVE,
                        "old-hash"
                );

        RotateAtmCredentialCommand command =
                new RotateAtmCredentialCommand(
                        terminalId
                );

        String newCredential =
                "NewSecureCredential123";

        String newCredentialHash =
                "new-hashed-credential";

        when(atmTerminalCommandRepository
                .findById(terminalId))
                .thenReturn(java.util.Optional.of(terminal));

        when(atmCredentialGenerator.generate())
                .thenReturn(newCredential);

        when(atmCredentialHasher.hash(newCredential))
                .thenReturn(newCredentialHash);

        when(atmTerminalCommandRepository
                .save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RotateAtmCredentialResponse response =
                handler.execute(command);

        assertThat(response)
                .isNotNull();

        assertThat(response.terminalId())
                .isEqualTo(terminalId);

        assertThat(response.terminalCode())
                .isEqualTo("ATM-HN-001");

        assertThat(response.credential())
                .isEqualTo(newCredential);

        assertThat(terminal.getCredentialHash())
                .isEqualTo(newCredentialHash);

        verify(atmTerminalCommandRepository)
                .findById(terminalId);

        verify(atmCredentialGenerator)
                .generate();

        verify(atmCredentialHasher)
                .hash(newCredential);

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
                atmTerminalCommandRepository,
                atmCredentialGenerator,
                atmCredentialHasher
        );
    }

    @Test
    void shouldRejectNullTerminalId() {
        RotateAtmCredentialCommand command =
                new RotateAtmCredentialCommand(null);

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(
                atmTerminalCommandRepository,
                atmCredentialGenerator,
                atmCredentialHasher
        );
    }

    @Test
    void shouldThrowWhenAtmTerminalNotFound() {
        UUID terminalId = UUID.randomUUID();

        RotateAtmCredentialCommand command =
                new RotateAtmCredentialCommand(
                        terminalId
                );

        when(atmTerminalCommandRepository
                .findById(terminalId))
                .thenReturn(java.util.Optional.empty());

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

        verifyNoInteractions(
                atmCredentialGenerator,
                atmCredentialHasher
        );
    }

    @Test
    void shouldReplaceOldCredentialHash() {
        UUID terminalId = UUID.randomUUID();

        AtmTerminal terminal =
                createTerminal(
                        terminalId,
                        "ATM-HN-001",
                        AtmTerminalStatus.ACTIVE,
                        "old-hash"
                );

        when(atmTerminalCommandRepository
                .findById(terminalId))
                .thenReturn(java.util.Optional.of(terminal));

        when(atmCredentialGenerator.generate())
                .thenReturn("new-credential");

        when(atmCredentialHasher.hash("new-credential"))
                .thenReturn("new-hash");

        when(atmTerminalCommandRepository
                .save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        handler.execute(
                new RotateAtmCredentialCommand(
                        terminalId
                )
        );

        assertThat(terminal.getCredentialHash())
                .isNotEqualTo("old-hash");

        assertThat(terminal.getCredentialHash())
                .isEqualTo("new-hash");
    }

    @Test
    void shouldStoreHashedCredentialNotPlainCredential() {
        UUID terminalId = UUID.randomUUID();

        AtmTerminal terminal =
                createTerminal(
                        terminalId,
                        "ATM-HN-001",
                        AtmTerminalStatus.ACTIVE,
                        "old-hash"
                );

        String plainCredential =
                "NewPlainCredential123";

        String hashedCredential =
                "hashed-new-credential";

        when(atmTerminalCommandRepository
                .findById(terminalId))
                .thenReturn(java.util.Optional.of(terminal));

        when(atmCredentialGenerator.generate())
                .thenReturn(plainCredential);

        when(atmCredentialHasher.hash(plainCredential))
                .thenReturn(hashedCredential);

        when(atmTerminalCommandRepository
                .save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        handler.execute(
                new RotateAtmCredentialCommand(
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

        assertThat(savedTerminal.getCredentialHash())
                .isEqualTo(hashedCredential);

        assertThat(savedTerminal.getCredentialHash())
                .isNotEqualTo(plainCredential);
    }

    @Test
    void shouldReturnNewCredentialOnly() {
        UUID terminalId = UUID.randomUUID();

        AtmTerminal terminal =
                createTerminal(
                        terminalId,
                        "ATM-HN-001",
                        AtmTerminalStatus.ACTIVE,
                        "old-hash"
                );

        when(atmTerminalCommandRepository
                .findById(terminalId))
                .thenReturn(java.util.Optional.of(terminal));

        when(atmCredentialGenerator.generate())
                .thenReturn("new-credential");

        when(atmCredentialHasher.hash("new-credential"))
                .thenReturn("new-hash");

        when(atmTerminalCommandRepository
                .save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RotateAtmCredentialResponse response =
                handler.execute(
                        new RotateAtmCredentialCommand(
                                terminalId
                        )
                );

        assertThat(response.credential())
                .isEqualTo("new-credential");

        assertThat(response.credential())
                .isNotEqualTo("old-hash");

        assertThat(response.terminalId())
                .isEqualTo(terminalId);

        assertThat(response.terminalCode())
                .isEqualTo("ATM-HN-001");
    }

    @Test
    void shouldRotateCredentialForInactiveTerminal() {
        UUID terminalId = UUID.randomUUID();

        AtmTerminal terminal =
                createTerminal(
                        terminalId,
                        "ATM-HN-002",
                        AtmTerminalStatus.INACTIVE,
                        "old-hash"
                );

        when(atmTerminalCommandRepository
                .findById(terminalId))
                .thenReturn(java.util.Optional.of(terminal));

        when(atmCredentialGenerator.generate())
                .thenReturn("new-credential");

        when(atmCredentialHasher.hash("new-credential"))
                .thenReturn("new-hash");

        when(atmTerminalCommandRepository
                .save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RotateAtmCredentialResponse response =
                handler.execute(
                        new RotateAtmCredentialCommand(
                                terminalId
                        )
                );

        assertThat(response.credential())
                .isEqualTo("new-credential");

        assertThat(terminal.getCredentialHash())
                .isEqualTo("new-hash");

        assertThat(terminal.getStatus())
                .isEqualTo(AtmTerminalStatus.INACTIVE);

        verify(atmTerminalCommandRepository)
                .save(terminal);
    }

    @Test
    void shouldNotChangeTerminalCodeWhenRotatingCredential() {
        UUID terminalId = UUID.randomUUID();

        AtmTerminal terminal =
                createTerminal(
                        terminalId,
                        "ATM-HN-001",
                        AtmTerminalStatus.ACTIVE,
                        "old-hash"
                );

        when(atmTerminalCommandRepository
                .findById(terminalId))
                .thenReturn(java.util.Optional.of(terminal));

        when(atmCredentialGenerator.generate())
                .thenReturn("new-credential");

        when(atmCredentialHasher.hash("new-credential"))
                .thenReturn("new-hash");

        when(atmTerminalCommandRepository
                .save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        handler.execute(
                new RotateAtmCredentialCommand(
                        terminalId
                )
        );

        assertThat(terminal.getTerminalCode())
                .isEqualTo("ATM-HN-001");
    }

    private AtmTerminal createTerminal(
            UUID id,
            String terminalCode,
            AtmTerminalStatus status,
            String credentialHash
    ) {
        return AtmTerminal.builder()
                .id(id)
                .terminalCode(terminalCode)
                .credentialHash(credentialHash)
                .status(status)
                .location("Hanoi")
                .build();
    }
}