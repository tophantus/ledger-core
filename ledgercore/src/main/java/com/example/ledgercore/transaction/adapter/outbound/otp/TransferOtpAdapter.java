package com.example.ledgercore.transaction.adapter.outbound.otp;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.otp.command.dto.SendOtpCommand;
import com.example.ledgercore.otp.command.dto.VerifyOtpCommand;
import com.example.ledgercore.otp.command.port.inbound.SendOtpUseCase;
import com.example.ledgercore.otp.command.port.inbound.VerifyOtpUseCase;
import com.example.ledgercore.otp.enums.OtpChannel;
import com.example.ledgercore.otp.enums.OtpPurpose;
import com.example.ledgercore.otp.enums.OtpStatus;
import com.example.ledgercore.transaction.command.port.outbound.TransferOtpPort;
import com.example.ledgercore.user.query.dto.UserEmailResponse;
import com.example.ledgercore.user.query.port.inbound.GetUserEmailUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransferOtpAdapter
        implements TransferOtpPort {

    private final SendOtpUseCase sendOtpUseCase;
    private final VerifyOtpUseCase verifyOtpUseCase;
    private final GetUserEmailUseCase getUserEmailUseCase;

    @Override
    public void sendConfirmationOtp(
            UUID userId,
            UUID transferIntentId
    ) {
        UserEmailResponse userEmail =
                getUserEmailUseCase.execute(userId);

        sendOtpUseCase.execute(
                new SendOtpCommand(
                        userId,
                        transferIntentId,
                        OtpPurpose.CONFIRM_TRANSFER,
                        OtpChannel.EMAIL,
                        userEmail.email()
                )
        );
    }

    @Override
    public void verifyConfirmationOtp(
            UUID userId,
            UUID transferIntentId,
            String otp
    ) {
        OtpStatus status =
                verifyOtpUseCase.execute(
                        new VerifyOtpCommand(
                                userId,
                                transferIntentId,
                                OtpPurpose.CONFIRM_TRANSFER,
                                otp
                        )
                );

        if (status != OtpStatus.VERIFIED) {
            throw new BusinessException(
                    getErrorCode(status)
            );
        }
    }

    private ErrorCode getErrorCode(OtpStatus status) {
        return switch (status) {
            case EXPIRED -> ErrorCode.OTP_EXPIRED;
            case LOCKED -> ErrorCode.OTP_LOCKED;
            case PENDING -> ErrorCode.OTP_INVALID;
            case VERIFIED -> throw new IllegalStateException(
                    "Verified OTP must not be mapped to an error"
            );
        };
    }
}