package com.example.ledgercore.otp.command.handler;

import com.example.ledgercore.otp.command.dto.SendOtpCommand;
import com.example.ledgercore.otp.command.port.inbound.SendOtpUseCase;
import com.example.ledgercore.otp.command.port.outbound.OtpSenderPort;
import com.example.ledgercore.otp.entity.OtpChallenge;
import com.example.ledgercore.otp.enums.OtpStatus;
import com.example.ledgercore.otp.command.repository.OtpCommandRepository;
import com.example.ledgercore.otp.service.OtpGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SendOtpHandler implements SendOtpUseCase {

    private static final long OTP_EXPIRATION_SECONDS = 300;

    private final OtpCommandRepository otpCommandRepository;
    private final OtpGenerator otpGenerator;
    private final OtpSenderPort otpSenderPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void execute(SendOtpCommand command) {

        String otp = otpGenerator.generate();

        Instant now = Instant.now();

        OtpChallenge challenge = OtpChallenge.builder()
                .subjectId(command.subjectId())
                .referenceId(command.referenceId())
                .purpose(command.purpose())
                .channel(command.channel())
                .destination(command.destination())
                .otpHash(passwordEncoder.encode(otp))
                .status(OtpStatus.PENDING)
                .attempts(0)
                .expiresAt(
                        now.plusSeconds(OTP_EXPIRATION_SECONDS)
                )
                .build();

        otpCommandRepository.save(challenge);

        otpSenderPort.send(
                command.purpose(),
                command.channel(),
                command.destination(),
                otp
        );
    }
}