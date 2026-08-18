package com.example.ledgercore.otp.adapter.outbound;

import com.example.ledgercore.auth.command.port.outbound.OtpVerificationPort;
import com.example.ledgercore.otp.command.dto.SendOtpCommand;
import com.example.ledgercore.otp.command.dto.VerifyOtpCommand;
import com.example.ledgercore.otp.command.port.inbound.SendOtpUseCase;
import com.example.ledgercore.otp.command.port.inbound.VerifyOtpUseCase;
import com.example.ledgercore.otp.enums.OtpChannel;
import com.example.ledgercore.otp.enums.OtpPurpose;
import com.example.ledgercore.otp.enums.OtpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OtpVerificationAdapter
        implements OtpVerificationPort {

    private final SendOtpUseCase sendOtpUseCase;
    private final VerifyOtpUseCase verifyOtpUseCase;

    @Override
    public void sendSignupOtp(
            UUID userId,
            String destination
    ) {
        sendOtpUseCase.execute(
                new SendOtpCommand(
                        userId,
                        null,
                        OtpPurpose.SIGNUP,
                        OtpChannel.EMAIL,
                        destination
                )
        );
    }

    @Override
    public VerificationResult verifySignupOtp(
            UUID userId,
            String otp
    ) {
        OtpStatus result = verifyOtpUseCase.execute(
                new VerifyOtpCommand(
                        userId,
                        null,
                        OtpPurpose.SIGNUP,
                        otp
                )
        );

        return new VerificationResult(
                mapStatus(result)
        );
    }

    private Status mapStatus(OtpStatus status) {
        return switch (status) {
            case VERIFIED ->
                    Status.VERIFIED;

            case EXPIRED ->
                    Status.EXPIRED;

            case PENDING, LOCKED ->
                    Status.INVALID;
        };
    }
}