package com.example.ledgercore.otp.command.handler;

import com.example.ledgercore.otp.command.dto.VerifyOtpCommand;
import com.example.ledgercore.otp.command.repository.OtpCommandRepository;
import com.example.ledgercore.otp.entity.OtpChallenge;
import com.example.ledgercore.otp.enums.OtpPurpose;
import com.example.ledgercore.otp.enums.OtpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyOtpHandlerTest {

    @Mock
    private OtpCommandRepository otpCommandRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpChallenge challenge;

    @InjectMocks
    private VerifyOtpHandler handler;

    private UUID subjectId;
    private UUID referenceId;

    @BeforeEach
    void setUp() {
        subjectId = UUID.randomUUID();
        referenceId = UUID.randomUUID();
    }

    private VerifyOtpCommand command(String otp) {
        return new VerifyOtpCommand(
                subjectId,
                referenceId,
                OtpPurpose.EMAIL_VERIFICATION,
                otp
        );
    }

    @Test
    void shouldReturnExpired_whenNoPendingOtpFound() {

        when(otpCommandRepository.findLatest(
                eq(subjectId),
                eq(referenceId),
                eq(OtpPurpose.EMAIL_VERIFICATION.name()),
                eq(OtpStatus.PENDING.name())
        )).thenReturn(Optional.empty());

        OtpStatus result =
                handler.execute(command("123456"));

        assertEquals(
                OtpStatus.EXPIRED,
                result
        );

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(challenge);
    }

    @Test
    void shouldExpireChallenge_whenOtpIsExpired() {

        when(otpCommandRepository.findLatest(
                any(),
                any(),
                any(),
                any()
        )).thenReturn(Optional.of(challenge));

        when(challenge.isExpired(any(Instant.class)))
                .thenReturn(true);

        OtpStatus result =
                handler.execute(command("123456"));

        assertEquals(
                OtpStatus.EXPIRED,
                result
        );

        verify(challenge)
                .expire();

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldReturnLocked_whenMaxAttemptsAlreadyReached() {

        when(otpCommandRepository.findLatest(
                any(),
                any(),
                any(),
                any()
        )).thenReturn(Optional.of(challenge));

        when(challenge.isExpired(any(Instant.class)))
                .thenReturn(false);

        when(challenge.getAttempts())
                .thenReturn(5);

        OtpStatus result =
                handler.execute(command("123456"));

        assertEquals(
                OtpStatus.LOCKED,
                result
        );

        verify(challenge)
                .lock();

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldReturnPending_whenOtpIsInvalid() {

        when(otpCommandRepository.findLatest(
                any(),
                any(),
                any(),
                any()
        )).thenReturn(Optional.of(challenge));

        when(challenge.isExpired(any(Instant.class)))
                .thenReturn(false);

        when(challenge.getAttempts())
                .thenReturn(0);

        when(challenge.getOtpHash())
                .thenReturn("hashed-otp");

        when(passwordEncoder.matches(
                eq("123456"),
                eq("hashed-otp")
        )).thenReturn(false);

        OtpStatus result =
                handler.execute(command("123456"));

        assertEquals(
                OtpStatus.PENDING,
                result
        );

        verify(challenge)
                .increaseAttempts();

        verify(challenge, never())
                .verify(any(Instant.class));

        verify(challenge, never())
                .lock();
    }

    @Test
    void shouldReturnLocked_whenInvalidOtpReachesMaxAttempts() {

        when(otpCommandRepository.findLatest(
                any(),
                any(),
                any(),
                any()
        )).thenReturn(Optional.of(challenge));

        when(challenge.isExpired(any(Instant.class)))
                .thenReturn(false);

        when(challenge.getAttempts())
                .thenReturn(4, 5);

        when(challenge.getOtpHash())
                .thenReturn("hashed-otp");

        when(passwordEncoder.matches(
                eq("123456"),
                eq("hashed-otp")
        )).thenReturn(false);

        OtpStatus result =
                handler.execute(command("123456"));

        assertEquals(
                OtpStatus.LOCKED,
                result
        );

        verify(challenge)
                .increaseAttempts();

        verify(challenge)
                .lock();

        verify(challenge, never())
                .verify(any(Instant.class));

        verify(challenge, never())
                .expire();
    }

    @Test
    void shouldReturnVerified_whenOtpIsCorrect() {

        when(otpCommandRepository.findLatest(
                any(),
                any(),
                any(),
                any()
        )).thenReturn(Optional.of(challenge));

        when(challenge.isExpired(any(Instant.class)))
                .thenReturn(false);

        when(challenge.getAttempts())
                .thenReturn(0);

        when(challenge.getOtpHash())
                .thenReturn("hashed-otp");

        when(passwordEncoder.matches(
                eq("123456"),
                eq("hashed-otp")
        )).thenReturn(true);

        OtpStatus result =
                handler.execute(command("123456"));

        assertEquals(
                OtpStatus.VERIFIED,
                result
        );

        verify(challenge)
                .verify(any(Instant.class));

        verify(challenge, never())
                .increaseAttempts();

        verify(challenge, never())
                .lock();

        verify(challenge, never())
                .expire();
    }

    @Test
    void shouldQueryLatestPendingOtp() {

        when(otpCommandRepository.findLatest(
                any(),
                any(),
                any(),
                any()
        )).thenReturn(Optional.of(challenge));

        when(challenge.isExpired(any(Instant.class)))
                .thenReturn(false);

        when(challenge.getAttempts())
                .thenReturn(0);

        when(challenge.getOtpHash())
                .thenReturn("hashed-otp");

        when(passwordEncoder.matches(
                eq("123456"),
                eq("hashed-otp")
        )).thenReturn(true);

        handler.execute(command("123456"));

        verify(otpCommandRepository)
                .findLatest(
                        subjectId,
                        referenceId,
                        OtpPurpose.EMAIL_VERIFICATION.name(),
                        OtpStatus.PENDING.name()
                );
    }

    @Test
    void shouldNotVerify_whenOtpIsInvalid() {

        when(otpCommandRepository.findLatest(
                any(),
                any(),
                any(),
                any()
        )).thenReturn(Optional.of(challenge));

        when(challenge.isExpired(any(Instant.class)))
                .thenReturn(false);

        when(challenge.getAttempts())
                .thenReturn(2);

        when(challenge.getOtpHash())
                .thenReturn("hashed-otp");

        when(passwordEncoder.matches(
                eq("wrong"),
                eq("hashed-otp")
        )).thenReturn(false);

        OtpStatus result =
                handler.execute(command("wrong"));

        assertEquals(
                OtpStatus.PENDING,
                result
        );

        verify(challenge)
                .increaseAttempts();

        verify(challenge, never())
                .verify(any(Instant.class));

        verify(challenge, never())
                .expire();

        verify(challenge, never())
                .lock();
    }
}