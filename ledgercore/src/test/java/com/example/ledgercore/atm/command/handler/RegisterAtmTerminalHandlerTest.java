package com.example.ledgercore.atm.command.handler;

import com.example.ledgercore.atm.command.dto.RegisterAtmTerminalCommand;
import com.example.ledgercore.atm.command.dto.RegisterAtmTerminalResponse;
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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterAtmTerminalHandlerTest {

    @Mock
    private AtmTerminalCommandRepository
            atmTerminalCommandRepository;

    @Mock
    private AtmCredentialGenerator
            atmCredentialGenerator;

    @Mock
    private AtmCredentialHasher
            atmCredentialHasher;

    private Clock clock;

    private RegisterAtmTerminalHandler handler;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                Instant.parse("2026-09-09T00:00:00Z"),
                ZoneOffset.UTC
        );

        handler = new RegisterAtmTerminalHandler(
                atmTerminalCommandRepository,
                atmCredentialGenerator,
                atmCredentialHasher,
                clock
        );
    }

    @Test
    void shouldRegisterAtmTerminalSuccessfully() {
        RegisterAtmTerminalCommand command =
                new RegisterAtmTerminalCommand(
                        "ATM-HN-001",
                        "Hanoi"
                );

        String credential =
                "aBc123SecureCredential";

        String credentialHash =
                "$2a$10$hashedCredential";

        UUID terminalId =
                UUID.randomUUID();

        when(atmTerminalCommandRepository
                .existsByTerminalCode("ATM-HN-001"))
                .thenReturn(false);

        when(atmCredentialGenerator.generate())
                .thenReturn(credential);

        when(atmCredentialHasher.hash(credential))
                .thenReturn(credentialHash);

        when(atmTerminalCommandRepository.save(any(AtmTerminal.class)))
                .thenAnswer(invocation -> {
                    AtmTerminal terminal =
                            invocation.getArgument(0);

                    terminal.setId(terminalId);

                    return terminal;
                });

        RegisterAtmTerminalResponse response =
                handler.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.id())
                .isEqualTo(terminalId);
        assertThat(response.terminalCode())
                .isEqualTo("ATM-HN-001");
        assertThat(response.credential())
                .isEqualTo(credential);
        assertThat(response.status())
                .isEqualTo(AtmTerminalStatus.ACTIVE);
        assertThat(response.location())
                .isEqualTo("Hanoi");
        assertThat(response.createdAt())
                .isEqualTo(
                        Instant.parse(
                                "2026-09-09T00:00:00Z"
                        )
                );

        verify(atmTerminalCommandRepository)
                .existsByTerminalCode("ATM-HN-001");

        verify(atmCredentialGenerator)
                .generate();

        verify(atmCredentialHasher)
                .hash(credential);

        verify(atmTerminalCommandRepository)
                .save(any(AtmTerminal.class));
    }

    @Test
    void shouldTrimTerminalCodeBeforeRegistering() {
        RegisterAtmTerminalCommand command =
                new RegisterAtmTerminalCommand(
                        "  ATM-HN-001  ",
                        "Hanoi"
                );

        String credential = "secureCredential";
        String credentialHash = "hashedCredential";

        when(atmTerminalCommandRepository
                .existsByTerminalCode("ATM-HN-001"))
                .thenReturn(false);

        when(atmCredentialGenerator.generate())
                .thenReturn(credential);

        when(atmCredentialHasher.hash(credential))
                .thenReturn(credentialHash);

        when(atmTerminalCommandRepository.save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RegisterAtmTerminalResponse response =
                handler.execute(command);

        assertThat(response.terminalCode())
                .isEqualTo("ATM-HN-001");

        ArgumentCaptor<AtmTerminal> captor =
                ArgumentCaptor.forClass(AtmTerminal.class);

        verify(atmTerminalCommandRepository)
                .save(captor.capture());

        assertThat(captor.getValue().getTerminalCode())
                .isEqualTo("ATM-HN-001");
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
    void shouldRejectNullTerminalCode() {
        RegisterAtmTerminalCommand command =
                new RegisterAtmTerminalCommand(
                        null,
                        "Hanoi"
                );

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
    void shouldRejectBlankTerminalCode() {
        RegisterAtmTerminalCommand command =
                new RegisterAtmTerminalCommand(
                        "   ",
                        "Hanoi"
                );

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
    void shouldRejectTerminalCodeLongerThan50Characters() {
        String terminalCode = "A".repeat(51);

        RegisterAtmTerminalCommand command =
                new RegisterAtmTerminalCommand(
                        terminalCode,
                        "Hanoi"
                );

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
    void shouldRejectLocationLongerThan255Characters() {
        String location = "A".repeat(256);

        RegisterAtmTerminalCommand command =
                new RegisterAtmTerminalCommand(
                        "ATM-HN-001",
                        location
                );

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
    void shouldAllowNullLocation() {
        RegisterAtmTerminalCommand command =
                new RegisterAtmTerminalCommand(
                        "ATM-HN-001",
                        null
                );

        String credential = "secureCredential";
        String credentialHash = "hashedCredential";

        when(atmTerminalCommandRepository
                .existsByTerminalCode("ATM-HN-001"))
                .thenReturn(false);

        when(atmCredentialGenerator.generate())
                .thenReturn(credential);

        when(atmCredentialHasher.hash(credential))
                .thenReturn(credentialHash);

        when(atmTerminalCommandRepository.save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RegisterAtmTerminalResponse response =
                handler.execute(command);

        assertThat(response.location())
                .isNull();
    }

    @Test
    void shouldRejectDuplicateTerminalCode() {
        RegisterAtmTerminalCommand command =
                new RegisterAtmTerminalCommand(
                        "ATM-HN-001",
                        "Hanoi"
                );

        when(atmTerminalCommandRepository
                .existsByTerminalCode("ATM-HN-001"))
                .thenReturn(true);

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(
                        ErrorCode.ATM_TERMINAL_ALREADY_EXISTS
                );

        verify(atmTerminalCommandRepository)
                .existsByTerminalCode("ATM-HN-001");

        verify(atmTerminalCommandRepository, never())
                .save(any());

        verifyNoInteractions(
                atmCredentialGenerator,
                atmCredentialHasher
        );
    }

    @Test
    void shouldCreateActiveTerminal() {
        RegisterAtmTerminalCommand command =
                new RegisterAtmTerminalCommand(
                        "ATM-HN-001",
                        "Hanoi"
                );

        when(atmTerminalCommandRepository
                .existsByTerminalCode("ATM-HN-001"))
                .thenReturn(false);

        when(atmCredentialGenerator.generate())
                .thenReturn("secureCredential");

        when(atmCredentialHasher.hash("secureCredential"))
                .thenReturn("hashedCredential");

        when(atmTerminalCommandRepository.save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        handler.execute(command);

        ArgumentCaptor<AtmTerminal> captor =
                ArgumentCaptor.forClass(AtmTerminal.class);

        verify(atmTerminalCommandRepository)
                .save(captor.capture());

        AtmTerminal terminal = captor.getValue();

        assertThat(terminal.getTerminalCode())
                .isEqualTo("ATM-HN-001");

        assertThat(terminal.getCredentialHash())
                .isEqualTo("hashedCredential");

        assertThat(terminal.getStatus())
                .isEqualTo(AtmTerminalStatus.ACTIVE);

        assertThat(terminal.getLocation())
                .isEqualTo("Hanoi");

        assertThat(terminal.getCreatedAt())
                .isEqualTo(
                        Instant.parse(
                                "2026-09-09T00:00:00Z"
                        )
                );

        assertThat(terminal.getUpdatedAt())
                .isEqualTo(
                        Instant.parse(
                                "2026-09-09T00:00:00Z"
                        )
                );
    }

    @Test
    void shouldStoreHashedCredentialInsteadOfPlainCredential() {
        RegisterAtmTerminalCommand command =
                new RegisterAtmTerminalCommand(
                        "ATM-HN-001",
                        "Hanoi"
                );

        String plainCredential =
                "plainCredential";

        String credentialHash =
                "hashedCredential";

        when(atmTerminalCommandRepository
                .existsByTerminalCode("ATM-HN-001"))
                .thenReturn(false);

        when(atmCredentialGenerator.generate())
                .thenReturn(plainCredential);

        when(atmCredentialHasher.hash(plainCredential))
                .thenReturn(credentialHash);

        when(atmTerminalCommandRepository.save(any(AtmTerminal.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        handler.execute(command);

        ArgumentCaptor<AtmTerminal> captor =
                ArgumentCaptor.forClass(AtmTerminal.class);

        verify(atmTerminalCommandRepository)
                .save(captor.capture());

        AtmTerminal terminal = captor.getValue();

        assertThat(terminal.getCredentialHash())
                .isEqualTo(credentialHash);

        assertThat(terminal.getCredentialHash())
                .isNotEqualTo(plainCredential);
    }
}