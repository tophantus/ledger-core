package com.example.ledgercore.otp.command.handler;

import com.example.ledgercore.otp.command.dto.VerifyOtpCommand;
import com.example.ledgercore.otp.command.port.inbound.VerifyOtpUseCase;
import com.example.ledgercore.otp.command.repository.OtpCommandRepository;
import com.example.ledgercore.otp.entity.OtpChallenge;
import com.example.ledgercore.otp.enums.OtpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class VerifyOtpHandler implements VerifyOtpUseCase {

    private static final int MAX_ATTEMPTS = 5;

    private final OtpCommandRepository otpCommandRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OtpStatus execute(VerifyOtpCommand command) {

        OtpChallenge challenge =
                otpCommandRepository.findLatest(
                        command.subjectId(),
                        command.referenceId(),
                        command.purpose().name(),
                        OtpStatus.PENDING.name()
                ).orElse(null);

        if (challenge == null) {
            return OtpStatus.EXPIRED;
        }

        Instant now = Instant.now();

        if (challenge.isExpired(now)) {
            challenge.expire();
            return OtpStatus.EXPIRED;
        }

        if (challenge.getAttempts() >= MAX_ATTEMPTS) {
            challenge.lock();
            return OtpStatus.LOCKED;
        }

        if (!passwordEncoder.matches(
                command.otp(),
                challenge.getOtpHash()
        )) {
            challenge.increaseAttempts();

            if (challenge.getAttempts() >= MAX_ATTEMPTS) {
                challenge.lock();
                return OtpStatus.LOCKED;
            }

            return OtpStatus.PENDING;
        }

        challenge.verify(now);

        return OtpStatus.VERIFIED;
    }
}