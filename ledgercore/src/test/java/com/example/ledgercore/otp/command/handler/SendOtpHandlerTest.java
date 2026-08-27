package com.example.ledgercore.otp.command.handler;

import com.example.ledgercore.otp.command.dto.SendOtpCommand;
import com.example.ledgercore.otp.command.port.outbound.OtpSenderPort;
import com.example.ledgercore.otp.command.repository.OtpCommandRepository;
import com.example.ledgercore.otp.entity.OtpChallenge;
import com.example.ledgercore.otp.enums.OtpChannel;
import com.example.ledgercore.otp.enums.OtpPurpose;
import com.example.ledgercore.otp.enums.OtpStatus;
import com.example.ledgercore.otp.service.OtpGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendOtpHandlerTest {

    @Mock
    private OtpCommandRepository otpCommandRepository;

    @Mock
    private OtpGenerator otpGenerator;

    @Mock
    private OtpSenderPort otpSenderPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SendOtpHandler handler;

    private UUID subjectId;
    private UUID referenceId;
    private String destination;
    private String otp;

    @BeforeEach
    void setUp() {
        subjectId = UUID.randomUUID();
        referenceId = UUID.randomUUID();
        destination = "test@example.com";
        otp = "123456";
    }

    @Test
    void shouldCreateAndSendOtp() {
        SendOtpCommand command =
                new SendOtpCommand(
                        subjectId,
                        referenceId,
                        OtpPurpose.EMAIL_VERIFICATION,
                        OtpChannel.EMAIL,
                        destination
                );

        UUID otpId = UUID.randomUUID();

        when(otpGenerator.generate())
                .thenReturn(otp);

        when(passwordEncoder.encode(otp))
                .thenReturn("hashed-otp");

        when(otpCommandRepository.save(any(OtpChallenge.class)))
                .thenAnswer(invocation -> {
                    OtpChallenge challenge = invocation.getArgument(0);

                    return challenge;
                });

        handler.execute(command);

        ArgumentCaptor<OtpChallenge> captor =
                ArgumentCaptor.forClass(OtpChallenge.class);

        verify(otpCommandRepository)
                .save(captor.capture());

        OtpChallenge challenge = captor.getValue();

        assertEquals(subjectId, challenge.getSubjectId());
        assertEquals(referenceId, challenge.getReferenceId());
        assertEquals(
                OtpPurpose.EMAIL_VERIFICATION,
                challenge.getPurpose()
        );
        assertEquals(
                OtpChannel.EMAIL,
                challenge.getChannel()
        );
        assertEquals(destination, challenge.getDestination());
        assertEquals("hashed-otp", challenge.getOtpHash());
        assertEquals(OtpStatus.PENDING, challenge.getStatus());
        assertEquals(0, challenge.getAttempts());

        assertNotNull(challenge.getExpiresAt());
        assertTrue(
                challenge.getExpiresAt()
                        .isAfter(Instant.now())
        );

        verify(otpGenerator)
                .generate();

        verify(passwordEncoder)
                .encode(otp);

        verify(otpSenderPort)
                .send(
                        any(),
                        eq(subjectId),
                        eq(referenceId),
                        eq(OtpPurpose.EMAIL_VERIFICATION),
                        eq(OtpChannel.EMAIL),
                        eq(destination),
                        eq(otp)
                );
    }

    @Test
    void shouldEncodeGeneratedOtpBeforeSaving() {
        SendOtpCommand command =
                new SendOtpCommand(
                        subjectId,
                        referenceId,
                        OtpPurpose.EMAIL_VERIFICATION,
                        OtpChannel.EMAIL,
                        destination
                );

        when(otpGenerator.generate())
                .thenReturn(otp);

        when(passwordEncoder.encode(otp))
                .thenReturn("encoded");

        when(otpCommandRepository.save(any(OtpChallenge.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        handler.execute(command);

        verify(passwordEncoder)
                .encode(otp);

        ArgumentCaptor<OtpChallenge> captor =
                ArgumentCaptor.forClass(OtpChallenge.class);

        verify(otpCommandRepository)
                .save(captor.capture());

        assertEquals(
                "encoded",
                captor.getValue().getOtpHash()
        );
    }

    @Test
    void shouldUsePurposeExpirationForExpiryTime() {
        SendOtpCommand command =
                new SendOtpCommand(
                        subjectId,
                        referenceId,
                        OtpPurpose.EMAIL_VERIFICATION,
                        OtpChannel.EMAIL,
                        destination
                );

        Instant before = Instant.now();

        when(otpGenerator.generate())
                .thenReturn(otp);

        when(passwordEncoder.encode(otp))
                .thenReturn("encoded");

        when(otpCommandRepository.save(any(OtpChallenge.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        handler.execute(command);

        Instant after = Instant.now();

        ArgumentCaptor<OtpChallenge> captor =
                ArgumentCaptor.forClass(OtpChallenge.class);

        verify(otpCommandRepository)
                .save(captor.capture());

        Instant expiresAt =
                captor.getValue().getExpiresAt();

        Instant expectedMin =
                before.plus(command.purpose().getExpiration());

        Instant expectedMax =
                after.plus(command.purpose().getExpiration());

        assertFalse(expiresAt.isBefore(expectedMin));
        assertFalse(expiresAt.isAfter(expectedMax));
    }

    @Test
    void shouldSendGeneratedOtpNotHash() {
        SendOtpCommand command =
                new SendOtpCommand(
                        subjectId,
                        referenceId,
                        OtpPurpose.EMAIL_VERIFICATION,
                        OtpChannel.EMAIL,
                        destination
                );

        when(otpGenerator.generate())
                .thenReturn(otp);

        when(passwordEncoder.encode(otp))
                .thenReturn("hashed-value");

        when(otpCommandRepository.save(any(OtpChallenge.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        handler.execute(command);

        verify(otpSenderPort)
                .send(
                        any(),
                        eq(subjectId),
                        eq(referenceId),
                        eq(OtpPurpose.EMAIL_VERIFICATION),
                        eq(OtpChannel.EMAIL),
                        eq(destination),
                        eq(otp)
                );

        verify(otpSenderPort, never())
                .send(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        eq("hashed-value")
                );
    }
}