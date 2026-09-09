package com.example.ledgercore.atm.command.handler;

import com.example.ledgercore.atm.command.dto.AtmAuthenticationResult;
import com.example.ledgercore.atm.command.dto.AuthenticateAtmCommand;
import com.example.ledgercore.atm.command.repository.AtmTerminalCommandRepository;
import com.example.ledgercore.atm.command.service.AtmCredentialHasher;
import com.example.ledgercore.atm.entity.AtmTerminal;
import com.example.ledgercore.atm.enums.AtmTerminalStatus;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateAtmHandlerTest {

    @Mock
    private AtmTerminalCommandRepository
            atmTerminalCommandRepository;

    @Mock
    private AtmCredentialHasher
            atmCredentialHasher;

    @InjectMocks
    private AuthenticateAtmHandler handler;

    @Test
    void shouldAuthenticateAtmSuccessfully() {
        UUID terminalId = UUID.randomUUID();

        AuthenticateAtmCommand command =
                new AuthenticateAtmCommand(
                        "ATM-HN-001",
                        "secret"
                );

        AtmTerminal terminal =
                AtmTerminal.builder()
                        .id(terminalId)
                        .terminalCode("ATM-HN-001")
                        .credentialHash("hashed-secret")
                        .status(AtmTerminalStatus.ACTIVE)
                        .build();

        when(atmTerminalCommandRepository.findByTerminalCode(
                command.terminalCode()
        )).thenReturn(Optional.of(terminal));

        when(atmCredentialHasher.matches(
                command.credential(),
                terminal.getCredentialHash()
        )).thenReturn(true);

        AtmAuthenticationResult result =
                handler.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.atmTerminalId())
                .isEqualTo(terminalId);

        verify(atmTerminalCommandRepository)
                .findByTerminalCode("ATM-HN-001");

        verify(atmCredentialHasher)
                .matches(
                        "secret",
                        "hashed-secret"
                );
    }

    @Test
    void shouldThrowWhenAtmTerminalNotFound() {
        AuthenticateAtmCommand command =
                new AuthenticateAtmCommand(
                        "ATM-HN-001",
                        "secret"
                );

        when(atmTerminalCommandRepository.findByTerminalCode(
                command.terminalCode()
        )).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ATM_TERMINAL_NOT_FOUND);

        verify(atmTerminalCommandRepository)
                .findByTerminalCode("ATM-HN-001");

        verifyNoInteractions(atmCredentialHasher);
    }

    @Test
    void shouldThrowWhenAtmTerminalIsNotActive() {
        AuthenticateAtmCommand command =
                new AuthenticateAtmCommand(
                        "ATM-HN-001",
                        "secret"
                );

        AtmTerminal terminal =
                AtmTerminal.builder()
                        .id(UUID.randomUUID())
                        .terminalCode("ATM-HN-001")
                        .credentialHash("hashed-secret")
                        .status(AtmTerminalStatus.INACTIVE)
                        .build();

        when(atmTerminalCommandRepository.findByTerminalCode(
                command.terminalCode()
        )).thenReturn(Optional.of(terminal));

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ATM_TERMINAL_NOT_ACTIVE);

        verify(atmTerminalCommandRepository)
                .findByTerminalCode("ATM-HN-001");

        verifyNoInteractions(atmCredentialHasher);
    }

    @Test
    void shouldThrowWhenCredentialIsInvalid() {
        AuthenticateAtmCommand command =
                new AuthenticateAtmCommand(
                        "ATM-HN-001",
                        "wrong-secret"
                );

        AtmTerminal terminal =
                AtmTerminal.builder()
                        .id(UUID.randomUUID())
                        .terminalCode("ATM-HN-001")
                        .credentialHash("hashed-secret")
                        .status(AtmTerminalStatus.ACTIVE)
                        .build();

        when(atmTerminalCommandRepository.findByTerminalCode(
                command.terminalCode()
        )).thenReturn(Optional.of(terminal));

        when(atmCredentialHasher.matches(
                command.credential(),
                terminal.getCredentialHash()
        )).thenReturn(false);

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ATM_AUTHENTICATION_FAILED);

        verify(atmCredentialHasher)
                .matches(
                        "wrong-secret",
                        "hashed-secret"
                );
    }

    @Test
    void shouldThrowWhenCommandIsNull() {
        assertThatThrownBy(
                () -> handler.execute(null)
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(
                atmTerminalCommandRepository,
                atmCredentialHasher
        );
    }

    @Test
    void shouldThrowWhenTerminalCodeIsBlank() {
        AuthenticateAtmCommand command =
                new AuthenticateAtmCommand(
                        " ",
                        "secret"
                );

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(
                atmTerminalCommandRepository,
                atmCredentialHasher
        );
    }

    @Test
    void shouldThrowWhenCredentialIsBlank() {
        AuthenticateAtmCommand command =
                new AuthenticateAtmCommand(
                        "ATM-HN-001",
                        " "
                );

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(
                atmTerminalCommandRepository,
                atmCredentialHasher
        );
    }
}